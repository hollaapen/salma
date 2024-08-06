package com.leave.management.ui.screens.employee




import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.leave.management.R


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAccount(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val email = sharedPreferences.getString("user_email", "") ?: ""

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userMobile by remember { mutableStateOf("") }
    var userDesignation by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var userDob by remember { mutableStateOf("") }
    var userImage by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            db.collection("employees")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        userName = document.getString("name") ?: ""
                        userEmail = document.getString("email") ?: ""
                        userMobile = document.getString("mobilenumber") ?: ""
                        userDesignation = document.getString("designation") ?: ""
                        userAddress = document.getString("address") ?: ""
                        userDob = document.getString("dob") ?: ""
                        userImage = document.getString("image_url") ?: ""
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = {
                            passwordError = ""
                            when {
                                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                                    passwordError = "All fields are required"
                                }
                                newPassword != confirmPassword -> {
                                    passwordError = "Passwords do not match"
                                }
                                else -> {
                                    updatePassword(
                                        currentPassword,
                                        newPassword,
                                        onSuccess = {
                                            showDialog = false
                                            passwordError = ""
                                            Toast.makeText(context, "Password reset successful", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { error ->
                                            passwordError = error
                                        }
                                    )
                                }
                            }
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Account", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle navigation icon click */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.home), // Replace with your icon
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { userlogout(context, navController) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logout), // Replace with your icon
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFF8d58f0))
            )
        },
        bottomBar = { EmployeeBottomBar(navController = navController) }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxSize()
                .background(Color(0xFF8d58f0)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(top = 72.dp)
                    ) {
                        Spacer(modifier = Modifier.height(90.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color(0xff6f2dc2)),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = rememberImagePainter(data = userImage), // Replace with your image URL
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clip(CircleShape).size(100.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = userDesignation,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Text(
                                text = userMobile,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStat(label = "16", icon = R.drawable.leaveicon2) // Replace with your icon
                                ProfileStat(label = "18", icon = R.drawable.leaveicon2) // Replace with your icon
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "About Me",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries",
                        color = Color.Black,
                        fontSize = 14.sp
                    )



                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Skill",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Text(
                    text = "click here to change password",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { showDialog = true }
                )
            }
        }
    }
}

fun updatePassword(
    currentPassword: String,
    newPassword: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: return onFailure("User not logged in")

    val credential = EmailAuthProvider.getCredential(email, currentPassword)

    user.reauthenticate(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            onSuccess()
                        } else {
                            onFailure("Failed to update password")
                        }
                    }
            } else {
                onFailure("Current password do not match")
            }
        }
}

@Composable
fun ProfileStat(label: String, icon: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(text = label, color = Color.White, fontSize = 16.sp)
    }
}