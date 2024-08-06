package com.leave.management.ui.screens.admin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.leave.management.R
import com.leave.management.navigation.ROUTE_ADMINDASBOARD
import com.leave.management.navigation.ROUTE_ADMINLOGIN
import com.leave.management.ui.screens.employee.EmployeeBottomBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAccount(navController: NavHostController) {
    val mContext = LocalContext.current
    val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val email = sharedPreferences.getString("loggedInUserEmail", "") ?: ""

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userMobile by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        userName = document.getString("name") ?: ""
                        userEmail = document.getString("email") ?: ""
                        userMobile = document.getString("mobileNumber") ?: ""
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                }
        }
    }

    fun changePassword() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        if (newPassword == confirmNewPassword) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(mContext, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                        showChangePasswordDialog = false
                                    } else {
                                        errorMessage = updateTask.exception?.message ?: "Password change failed"
                                    }
                                }
                        } else {
                            errorMessage = "Passwords do not match"
                        }
                    } else {
                        errorMessage = "Current password does not match"
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Account", color = Color.White)
                },
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
                    IconButton(onClick = { logout(mContext, navController) }) {
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
        bottomBar = { AdminNavBar(navController = navController) }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxSize()
                .background(Color(0xFF8d58f0)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Background white box
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

                    // Profile card
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
                                    painter = rememberImagePainter(data = "https://apensoftwares.co.ke/images/media/1669670270logo.png"), // Replace with your image URL
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(100.dp)
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
                                text = "Human Centered Designer",
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Text(
                                text = userMobile,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = userEmail,
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
                                AdminProfileStat(label = "16", icon = R.drawable.leaveicon2) // Replace with your icon
                                AdminProfileStat(label = "18", icon = R.drawable.leaveicon2) // Replace with your icon
                            }
                        }
                    }
                }

            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                        .clickable { showChangePasswordDialog = true }
                ) {
                    Text(
                        text = "Change Password",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Click here to change password",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                title = { Text(text = "Change Password") },
                text = {
                    Column {
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
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirm New Password") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { changePassword() },
                        colors = ButtonDefaults.buttonColors(Color.Green)


                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showChangePasswordDialog = false },
                        colors = ButtonDefaults.buttonColors(Color.Red)


                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminProfileStat(label: String, icon: Int) {
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

fun logout(mContext: Context, navController: NavHostController) {
    val sharedPreferences: SharedPreferences = mContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        remove("loggedInUserEmail")
        putBoolean("isLoggedIn", false)
        apply()
    }
    navController.navigate(ROUTE_ADMINLOGIN) {
        popUpTo(ROUTE_ADMINDASBOARD) { inclusive = true }
    }
}
