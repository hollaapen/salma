package com.leave.management.ui.screens.admin

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.leave.management.navigation.ROUTE_ADDEMPLOYEE
import com.leave.management.navigation.ROUTE_ADMINDASBOARD
import com.leave.management.navigation.ROUTE_ADMINSETTINGS
import com.leave.management.navigation.ROUTE_VIEWLEAVEs
import com.leave.management.navigation.ROUTE_VIEWEMPLOYEES
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class Employees(
    val address: String = "",
    val designation: String = "",
    val dob: String = "",
    val email: String = "",
    val mobilenumber: String = "",
    val name: String = "",
    val image_url: String = ""
)

suspend fun fetchEmployees(db: FirebaseFirestore = FirebaseFirestore.getInstance()): List<Employees> {
    val employeesList = mutableListOf<Employees>()
    val result = db.collection("employees").get().await()
    for (document in result) {
        val employee = document.toObject<Employees>()
        employeesList.add(employee)
    }
    return employeesList
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewEmployeesScreen(navController: NavHostController) {


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Registered Employees",
                            color = Color.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                },
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars),

            )
        },
        bottomBar = {
            AdminNavBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {


            EmployeesScreen()


        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EmployeesScreen() {
    val employees = remember { mutableStateListOf<Employees>() }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // Fetch employees when the screen is first composed
    LaunchedEffect(Unit) {
        loadEmployees(
            onSuccess = { fetchedEmployees ->
                employees.addAll(fetchedEmployees)
                isLoading = false
            },
            onError = {
                isLoading = false
                isError = true
                // Show snackbar in a coroutine
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Failed to load employees")
                }
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
//                        color = MaterialTheme.colorScheme.primary
                    )
                    isError -> Text(
                        text = "Failed to load employees",
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.bodyLarge
                    )
                    employees.isEmpty() -> Text(
                        text = "No employees found",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onBackground
                    )
                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(employees, key = { it.email }) { employee ->
                            EmployeeItem(employee = employee)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EmployeeItem(employee: Employees) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(employee.image_url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Cyan, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = employee.name,

                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = employee.email,


                )
                Text(
                    text = employee.mobilenumber,

                )
            }
        }
    }
}

suspend fun loadEmployees(
    onSuccess: (List<Employees>) -> Unit,
    onError: (Exception) -> Unit
) {
    try {
        delay(1000)
        val employees = fetchEmployees()
        onSuccess(employees)
    } catch (e: Exception) {
        onError(e)
    }
}
