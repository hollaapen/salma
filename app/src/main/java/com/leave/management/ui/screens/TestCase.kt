package com.leave.management.ui.screens
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import coil.compose.rememberImagePainter
//import com.leave.management.R
//
//
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EmployeeProfileScreen() {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF8d58f0)),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        CenterAlignedTopAppBar(
//            title = {
//                Text(text = "Profile", color = Color.White)
//            },
//            navigationIcon = {
//                IconButton(onClick = { /* Handle navigation icon click */ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.leaveicon2), // Replace with your icon
//                        contentDescription = null,
//                        tint = Color.White
//                    )
//                }
//            },
//            actions = {
//                IconButton(onClick = { /* Handle edit icon click */ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.leaveicon2), // Replace with your icon
//                        contentDescription = null,
//                        tint = Color.White
//                    )
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(Color(0xFF8d58f0))
////            backgroundColor = Color(0xFF833AB4)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            // Background white box
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        Color.White,
//                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
//                    )
//                    .padding(top = 72.dp)
//            ) {
//                Spacer(modifier = Modifier.height(90.dp))
//            }
//
//            // Profile card
//            // 6f2dc2
//            Card(
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(Color(0xff6f2dc2)),
////
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .padding(bottom = 32.dp)
//                    .fillMaxWidth()
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(16.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(120.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    ) {
//                        Image(
//                            painter = rememberImagePainter(data = "https://apensoftwares.co.ke/images/media/1669670270logo.png"), // Replace with your image URL
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier.clip(CircleShape)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Gal Shir",
//                        color = Color.White,
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Text(
//                        text = "Human Centered Designer",
//                        color = Color.White,
//                        fontSize = 16.sp
//                    )
//
//                    Text(
//                        text = "+75 6789 6578",
//                        color = Color.White,
//                        fontSize = 16.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Row(
//                        modifier = Modifier
//                            .padding(horizontal = 16.dp)
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        ProfileStat(label = "16", icon = R.drawable.leaveicon2) // Replace with your icon
//                        ProfileStat(label = "18", icon = R.drawable.leaveicon2) // Replace with your icon
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier
//                .padding(horizontal = 16.dp)
//                .clip(RoundedCornerShape(16.dp))
//                .background(Color.White)
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "About Me",
//                color = Color.Black,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Text(
//                text = "when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries",
//                color = Color.Black,
//                fontSize = 14.sp
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Skill",
//                color = Color.Black,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.padding(top = 8.dp)
//            ) {
//                SkillChip("Sketch")
//                SkillChip("Photoshop")
//                SkillChip("UX Design")
//            }
//        }
//    }
//}
//
//@Composable
//fun ProfileStat(label: String, icon: Int) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Icon(
//            painter = painterResource(id = icon),
//            contentDescription = null,
//            tint = Color.White,
//            modifier = Modifier.size(24.dp)
//        )
//        Text(text = label, color = Color.White, fontSize = 16.sp)
//    }
//}
//
//@Composable
//fun SkillChip(skill: String) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(16.dp))
//            .background(Color(0xFF833AB4))
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    ) {
//        Text(text = skill, color = Color.White, fontSize = 14.sp)
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//
//    EmployeeProfileScreen()
//}
//
