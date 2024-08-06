package com.leave.management.ui.screens.admin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.firestore.FirebaseFirestore
import com.leave.management.navigation.ROUTE_ADDEMPLOYEE
import com.leave.management.navigation.ROUTE_ADMINACCOUNT
import com.leave.management.navigation.ROUTE_ADMINDASBOARD
import com.leave.management.navigation.ROUTE_ADMINSETTINGS
import com.leave.management.navigation.ROUTE_VIEWEMPLOYEES
import com.leave.management.navigation.ROUTE_VIEWLEAVEs
import com.leave.management.ui.screens.employee.ApprovedScreen
import com.leave.management.ui.screens.employee.RejectedScreen
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AllLeaves(navController: NavHostController) {
    val tabs = listOf("PENDING", "APPROVED", "REJECTED")
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    val mContext = LocalContext.current
    val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val email = sharedPreferences.getString("loggedInUserEmail", "") ?: ""
    var userName by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        userName = document.getString("name") ?: ""
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                }
        }
    }

    Scaffold(
        bottomBar = {
            AdminNavBar(navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PendingLeave(navController, userName)
                    1 -> ApprovedScreen(navController)
                    2 -> RejectedScreen(navController)
                }
            }
        }
    }
}

@Composable
fun PendingLeave(navController: NavHostController, userName: String) {
    var pendingLeaves by remember { mutableStateOf<List<LeaveApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showRejectDialog by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }
    var selectedLeaveId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    // Fetch pending leaves
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchPendingLeav { leaves, error ->
                pendingLeaves = leaves ?: emptyList()
                isLoading = false
                // Handle error if necessary
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (pendingLeaves.isEmpty()) {
                Text(text = "No pending leaves", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(pendingLeaves) { leave ->
                        LeaveItem(
                            leave = leave,
                            onApprove = { leaveId ->
                                selectedLeaveId = leaveId
                                showApproveDialog = true
                            },
                            onReject = { leaveId ->
                                selectedLeaveId = leaveId
                                showRejectDialog = true
                            },
                            userName = userName // Pass the userName parameter here
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }


                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (showApproveDialog) {
            ShowApproveDialog(
                leaveId = selectedLeaveId,
                comment = comment,
                onCommentChange = { newComment -> comment = newComment },
                onApproveConfirmed = { success ->
                    showApproveDialog = false
                    if (success) {
                        Toast.makeText(context, "Leave approved", Toast.LENGTH_SHORT).show()
                        navController.navigate(ROUTE_VIEWLEAVEs)
                        fetchPendingLeav { leaves, _ ->
                            pendingLeaves = leaves ?: emptyList()
                        }
                    } else {
                        Toast.makeText(context, "Failed to approve leave", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showApproveDialog = false },
                userName = userName // Pass userName here
            )
        }

        if (showRejectDialog) {
            ShowRejectDialog(
                context = context,
                leaveId = selectedLeaveId,
                userName = userName,
                onRejectConfirmed = { success ->
                    showRejectDialog = false
                    if (success) {
                        Toast.makeText(context, "Leave rejected", Toast.LENGTH_SHORT).show()
                        fetchPendingLeav { leaves, _ ->
                            pendingLeaves = leaves ?: emptyList()
                        }
                    } else {
                        Toast.makeText(context, "Failed to reject leave", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showRejectDialog = false }
            )
        }
    }
}

@Composable
fun ShowRejectDialog(
    context: Context,
    leaveId: String,
    userName: String,
    onRejectConfirmed: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }
    var comment by remember { mutableStateOf("") }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onDismiss()
            },
            title = {
                Text(
                    text = "Reject Leave",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please provide a reason for rejection:",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                            cursorColor = MaterialTheme.colors.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comment.isNotEmpty()) {
                            rejectLea(leaveId, comment, userName) { success ->
                                onRejectConfirmed(success)
                            }
                            openDialog = false
                        } else {
                            Toast.makeText(context, "Comment is required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        openDialog = false
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
                ) {
                    Text("Cancel", color = MaterialTheme.colors.primary)
                }
            },
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ShowApproveDialog(
    leaveId: String,
    comment: String,
    userName: String,
    onCommentChange: (String) -> Unit,
    onApproveConfirmed: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onDismiss()
            },
            title = {
                Text("Approve Leave",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "Please provide add a comment",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = onCommentChange,
                        label = { Text("Comment") }
                    )
                }
            },
            confirmButton = {
                OutlinedButton(




                    onClick = {
                        if (comment.isNotBlank()) {

                            approveLea(leaveId, comment, userName) { success ->
                                onApproveConfirmed(success)
                            }
                            openDialog = false
                            } else {
                            Toast.makeText(context, "Comment is required", Toast.LENGTH_SHORT).show()


                        }
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        openDialog = false
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LeaveItem(leave: LeaveApplication, onApprove: (String) -> Unit, onReject: (String) -> Unit, userName: String) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = leave.proofUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Leave Type: ${leave.leaveType}",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "By: ${leave.applied_by}, ${leave.email}",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "From Date: ${leave.fromDate}", style = MaterialTheme.typography.body1)
                Text(text = "To Date: ${leave.toDate}", style = MaterialTheme.typography.body1)
                Text(text = "Number of Days: ${leave.numberOfDays}", style = MaterialTheme.typography.body1)
                Text(text = "Reason: ${leave.reason}", style = MaterialTheme.typography.body1)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showApproveDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Approve",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Approve", color = Color.White)
                    }

                    Button(
                        onClick = { onReject(leave.id) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reject",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reject", color = Color.White)
                    }
                }

                if (showApproveDialog) {
                    ShowApproveDialog(
                        leaveId = leave.id,
                        comment = comment,
                        userName = userName, // Pass the userName parameter here
                        onCommentChange = { newComment -> comment = newComment },
                        onApproveConfirmed = { success ->
                            showApproveDialog = false
                            onApprove(leave.id)
                        },
                        onDismiss = { showApproveDialog = false }
                    )
                }
            }
        }
    }
}

data class LeaveApplication(
    val id: String = "",
    val leaveType: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val numberOfDays: Int = 0,
    val reason: String = "",
    val proofUrl: String = "",
    val leaveStatus: String = "",
    val applied_by: String = "",
    val comment: String = "",
    val handled_by: String = "",
    val email: String = ""
)


private fun approveLea(leaveId: String, comment: String, userName: String, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .document(leaveId)
        .update(mapOf(
            "leaveStatus" to "approved",
            "comment" to comment,
            "handled_by" to userName
        ))
        .addOnSuccessListener {
            onResult(true)
        }
        .addOnFailureListener {
            onResult(false)
        }
}

private fun rejectLea(leaveId: String, comment: String, handledBy: String, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .document(leaveId)
        .update(mapOf(
            "leaveStatus" to "rejected",
            "comment" to comment,
            "handled_by" to handledBy
        ))
        .addOnSuccessListener {
            onResult(true)
        }
        .addOnFailureListener { 
            onResult(false)
        }
}

private fun fetchPendingLeav(onResult: (List<LeaveApplication>?, Exception?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .whereEqualTo("leaveStatus", "pending")
        .get()
        .addOnSuccessListener { result ->
            val leaves = result.documents.mapNotNull { document ->
                document.toObject(LeaveApplication::class.java)?.copy(id = document.id)
            }
            onResult(leaves, null)
        }
        .addOnFailureListener { exception ->
            onResult(null, exception)
        }
}
