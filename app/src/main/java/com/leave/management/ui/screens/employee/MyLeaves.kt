package com.leave.management.ui.screens.employee


import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.firestore.FirebaseFirestore
import com.leave.management.navigation.ROUTE_APPLY
import com.leave.management.navigation.ROUTE_EMPLOYEEACCOUNT
import com.leave.management.navigation.ROUTE_EMPLOYEESETTINGS
import com.leave.management.navigation.ROUTE_MYLEAVES
import kotlinx.coroutines.launch
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.leave.management.navigation.ROUTE_EMPLOYEEDASHBOARD
import com.leave.management.ui.screens.admin.LeaveApplication


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class
)
@Composable
fun MyLeaves(navController: NavHostController) {
    val tabs = listOf("PENDING", "APPROVED", "REJECTED")
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { EmployeeBottomBar(navController = navController) }
    ) {


            Column(
                modifier = Modifier.fillMaxSize()
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
                        0 -> PendingScreen(navController)
                        1 -> ApprovedScreen(navController)
                        2 -> RejectedScreen(navController)
                    }
                }
            }

    }
}

//pending leaves starts here
@Composable
fun PendingScreen(navController: NavHostController) {
    var pendingLeaves by remember { mutableStateOf<List<LeaveApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fetch pending leaves
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchPendingLeaves { leaves, error ->
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
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            if (pendingLeaves.isEmpty()) {
                Text(
                    text = "No pending leaves",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(pendingLeaves) { leave ->
                        LeaveItem(
                            leave = leave,
                            onApprove = { leaveId ->
                                coroutineScope.launch {
                                    approveLeave(leaveId) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Leave approved", Toast.LENGTH_SHORT).show()
                                            // Refresh the list after successful update
                                            fetchPendingLeaves { leaves, _ ->
                                                pendingLeaves = leaves ?: emptyList()
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to approve leave", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun LeaveItem(leave: LeaveApplication, onApprove: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card content
            Column {
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
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "From Date: ${leave.fromDate}", )
                Text(text = "To Date: ${leave.toDate}", )
                Text(text = "Number of Days: ${leave.numberOfDays}", )
                Text(text = "Reason: ${leave.reason}", )
            }

            // Yellow ribbon
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Yellow, shape = RoundedCornerShape(bottomEnd = 16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, Color.White, shape = RoundedCornerShape(bottomEnd = 16.dp))
            ) {
                Text(
                    text = "Pending",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
    }
}



private fun approveLeave(leaveId: String, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .document(leaveId)
        .update("leaveStatus", "approved")
        .addOnSuccessListener {
            onResult(true)
        }
        .addOnFailureListener { exception ->
            onResult(false)
        }
}


private fun fetchPendingLeaves(onResult: (List<LeaveApplication>?, Exception?) -> Unit) {
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


//pending leaves starts here



private fun fetchApprovedLeaves(onResult: (List<LeaveApplication>?, Exception?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .whereEqualTo("leaveStatus", "approved")
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


@Composable
fun ApprovedScreen(navController: NavHostController) {
    var approvedLeaves by remember { mutableStateOf<List<LeaveApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fetch approved leaves
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchApprovedLeaves { leaves, error ->
                if (error == null) {
                    approvedLeaves = leaves ?: emptyList()
                } else {
                    // Handle the error, e.g., show a Toast or a Snackbar
                    Toast.makeText(context, "Error fetching approved leaves: ${error.message}", Toast.LENGTH_LONG).show()
                }
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp) // Optional: add padding to ensure content doesn't touch edges
    ) {
        if (isLoading) {
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp) // Optional: add padding around the indicator
            )
        } else {
            if (approvedLeaves.isEmpty()) {
                Text(
                    text = "No approved leaves",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp) // Optional: add padding around the text
                )
            } else {
                // Display list of approved leaves
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize() // Ensure LazyColumn takes full available space
                        //.padding(bottom = 100.dp) // Optional: add padding around the list items
                ) {
                    items(approvedLeaves) { leave ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Display leave details
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(Color.White)
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
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "From Date: ${leave.fromDate}")
                                        Text(text = "To Date: ${leave.toDate}")
                                        Text(text = "Number of Days: ${leave.numberOfDays}")
                                        Text(text = "Reason: ${leave.reason}")
                                        Text(text = "Comment: ${leave.comment}")
                                        Text(text = "Served By: ${leave.handled_by}")
                                    }
                                }
                                // Green ribbon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Green, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Approved",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun RejectedScreen(navController: NavHostController) {
    var rejectedLeaves by remember { mutableStateOf<List<LeaveApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchRejectedLeaves { leaves, error ->
                if (error == null) {
                    rejectedLeaves = leaves ?: emptyList()
                } else {
                    Toast.makeText(context, "Error fetching rejected leaves: ${error.message}", Toast.LENGTH_LONG).show()
                }
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            if (rejectedLeaves.isEmpty()) {
                Text(
                    text = "No rejected leaves",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rejectedLeaves) { leave ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column {
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "From Date: ${leave.fromDate}")
                                    Text(text = "To Date: ${leave.toDate}")
                                    Text(text = "Number of Days: ${leave.numberOfDays}")
                                    Text(text = "Reason: ${leave.reason}")
                                    Text(text = "Comment: ${leave.comment}")
                                    Text(text = "Served By: ${leave.handled_by}")
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Red, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .shadow(2.dp, shape = RoundedCornerShape(4.dp))
                                ) {
                                    Text(
                                        text = "Rejected",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun fetchRejectedLeaves(onResult: (List<LeaveApplication>?, Exception?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("leave_applications")
        .whereEqualTo("leaveStatus", "rejected")
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

