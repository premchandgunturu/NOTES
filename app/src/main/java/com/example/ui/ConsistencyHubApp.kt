package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

// ==========================================
// CUSTOM KINETIC MOTION & SPRING MODIFIERS
// ==========================================

fun Modifier.bounceClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceSpring"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

// ==========================================
// GLASSMORPHISM CARD COMPONENT
// ==========================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    isLight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val borderCol = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val resolvedBorder = borderStroke ?: BorderStroke(1.dp, borderCol)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = bg
        ),
        border = resolvedBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLight) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

// ==========================================
// TOP LEVEL APP FLOW CONTAINER
// ==========================================

@Composable
fun ConsistencyHubApp(viewModel: ConsistencyHubViewModel) {
    val currentScreen by viewModel.appScreen.collectAsStateWithLifecycle()

    val isLight by viewModel.isLightMode.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isLight) Color(0xFFF8FAFC) else PureBlack
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                fadeOut(animationSpec = tween(350))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.Onboarding -> OnboardingScreen(viewModel)
                AppScreen.Dashboard -> DashboardScreen(viewModel)
            }
        }
    }
}

// ==========================================
// ONBOARDING SCREEN (SECURE LOGIN/REGISTER)
// ==========================================

@Composable
fun OnboardingScreen(viewModel: ConsistencyHubViewModel) {
    val userVal by viewModel.loginUsernameInput.collectAsStateWithLifecycle()
    val fullNameVal by viewModel.loginFullNameInput.collectAsStateWithLifecycle()
    val passVal by viewModel.loginPasswordInput.collectAsStateWithLifecycle()
    val errorState by viewModel.onboardingError.collectAsStateWithLifecycle()
    val successState by viewModel.onboardingSuccessMessage.collectAsStateWithLifecycle()

    var isRegisterMode by remember { mutableStateOf(false) }

    val isLight by viewModel.isLightMode.collectAsStateWithLifecycle()

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeBackground = if (isLight) Color(0xFFF8FAFC) else PureBlack
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground

    // Stellar orbital aura brush
    val auraBrush = Brush.radialGradient(
        colors = listOf(
            if (isLight) OrbitViolet.copy(alpha = 0.12f) else OrbitViolet.copy(alpha = 0.35f),
            if (isLight) SamsungBlue.copy(alpha = 0.08f) else SamsungBlue.copy(alpha = 0.15f),
            Color.Transparent
        ),
        center = Offset(500f, 200f),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBackground)
    ) {
        // Upper background aura glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(auraBrush)
        )

        // Theme toggler at top right
        IconButton(
            onClick = { viewModel.isLightMode.value = !isLight },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 24.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isLight) Color(0xFFE2E8F0) else Color(0x1F252528))
                .border(0.5.dp, themeBorder, CircleShape)
                .testTag("theme_toggle_onboarding")
        ) {
            Icon(
                imageVector = if (isLight) Icons.Default.NightsStay else Icons.Default.WbSunny,
                contentDescription = "Switch Theme",
                tint = if (isLight) OrbitViolet else PastelYellow,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Elegant high-tech branding header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isLight) Color.White else Color(0xFF1E293B))
                    .border(1.5.dp, SamsungBlue, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "App Icon Logo",
                    tint = SamsungBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Consistency Hub",
                color = themeTextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("app_title")
            )

            Text(
                text = "Secure study & productivity vault",
                color = themeTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Glassmoprhic Input Card Container
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_card"),
                isLight = isLight
            ) {
                Text(
                    text = if (isRegisterMode) "Create Studio Account" else "Welcome Back",
                    color = themeTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isRegisterMode) "Set up dynamic credentials in seconds" else "Log in to reactive session",
                    color = themeTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                )

                // Input field for username
                OutlinedTextField(
                    value = userVal,
                    onValueChange = { viewModel.loginUsernameInput.value = it },
                    label = { Text("Username") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeTextPrimary,
                        unfocusedTextColor = themeTextPrimary,
                        focusedBorderColor = SamsungBlue,
                        unfocusedBorderColor = themeBorder,
                        focusedContainerColor = if (isLight) Color(0xFFF1F5F9) else Color(0x1A000000),
                        unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else Color(0x06000000),
                        focusedLabelColor = SamsungBlue,
                        unfocusedLabelColor = themeTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = fullNameVal,
                        onValueChange = { viewModel.loginFullNameInput.value = it },
                        label = { Text("Display/Full Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeTextPrimary,
                            unfocusedTextColor = themeTextPrimary,
                            focusedBorderColor = SamsungBlue,
                            unfocusedBorderColor = themeBorder,
                            focusedContainerColor = if (isLight) Color(0xFFF1F5F9) else Color(0x1A000000),
                            unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else Color(0x06000000),
                            focusedLabelColor = SamsungBlue,
                            unfocusedLabelColor = themeTextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("full_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for password
                OutlinedTextField(
                    value = passVal,
                    onValueChange = { viewModel.loginPasswordInput.value = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeTextPrimary,
                        unfocusedTextColor = themeTextPrimary,
                        focusedBorderColor = SamsungBlue,
                        unfocusedBorderColor = themeBorder,
                        focusedContainerColor = if (isLight) Color(0xFFF1F5F9) else Color(0x1A000000),
                        unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else Color(0x06000000),
                        focusedLabelColor = SamsungBlue,
                        unfocusedLabelColor = themeTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // Error feedback state
                errorState?.let { err ->
                    Text(
                        text = err,
                        color = if (isLight) Color(0xFFDC2626) else PastelRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .testTag("onboarding_error")
                    )
                }

                // Success feedback state
                successState?.let { msg ->
                    Text(
                        text = msg,
                        color = ActiveEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .testTag("onboarding_success")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary transaction button (bouncy)
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.registerNewUser()
                        } else {
                            viewModel.loginUser()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegisterMode) OrbitViolet else SamsungBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Join Hub Profile" else "Access Hub Desktop",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle logic
                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        viewModel.onboardingError.value = null
                        viewModel.onboardingSuccessMessage.value = null
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("toggle_register_mode_button")
                ) {
                    Text(
                        text = if (isRegisterMode) "Already verified? Access profile" else "New to the Hub? Start tracking offline",
                        color = SamsungBlue,
                        fontSize = 13.sp,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Created by Premchand",
                    color = themeTextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// CORE WORKSPACE DASHBOARD
// ==========================================

@Composable
fun DashboardScreen(viewModel: ConsistencyHubViewModel) {
    val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeUser by viewModel.currentUsername.collectAsStateWithLifecycle()
    val activeUserFullName by viewModel.currentUserFullName.collectAsStateWithLifecycle()
    val randomQuote by viewModel.activeQuote.collectAsStateWithLifecycle()
    val isLight by viewModel.isLightMode.collectAsStateWithLifecycle()

    val oneUiVer by viewModel.oneUiVersion.collectAsStateWithLifecycle()
    val galaxyAiAnalysis by viewModel.galaxyAiAnalysis.collectAsStateWithLifecycle()
    val isGalaxyAiProcessing by viewModel.isGalaxyAiProcessing.collectAsStateWithLifecycle()

    var isAiHubExpanded by remember { mutableStateOf(false) }

    val greetingPair = viewModel.getDynamicGreetingAndIcon()

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val themeBackground = if (isLight) Color(0xFFF8FAFC) else PureBlack

    val headerAura = Brush.radialGradient(
        colors = listOf(
            (if (isLight) OrbitViolet.copy(alpha = 0.08f) else OrbitViolet.copy(alpha = 0.28f)),
            (if (isLight) SamsungBlue.copy(alpha = 0.05f) else SamsungBlue.copy(alpha = 0.15f)),
            Color.Transparent
        ),
        center = Offset(520f, -80f),
        radius = 900f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBackground)
    ) {
        // Glowing Orbital Ambient Radial Gradient Background at the top-header level
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(headerAura)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (greetingPair.second) {
                                        "wb_sunny" -> Icons.Default.WbSunny
                                        "adjust" -> Icons.Default.Adjust
                                        "nights_stay" -> Icons.Default.NightsStay
                                        else -> Icons.Default.Bedtime
                                    },
                                    contentDescription = "Solar Lunar Icon",
                                    tint = if (greetingPair.second == "wb_sunny") PastelYellow else OrbitViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = greetingPair.first,
                                    color = themeTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeUserFullName ?: "Premchand Gunturu",
                                color = themeTextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        // Theme Toggling button at top-right
                        IconButton(
                            onClick = { viewModel.isLightMode.value = !isLight },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFFE2E8F0) else Color(0x1F252528))
                                .border(0.5.dp, themeBorder, CircleShape)
                                .testTag("theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isLight) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                contentDescription = "Switch Theme",
                                tint = if (isLight) OrbitViolet else PastelYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Large premium Samsung One UI-style Motivation Hero Card
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClickable { viewModel.rotateQuote() }
                            .testTag("quote_banner"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF141A24)
                        ),
                        border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x22FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "“",
                                color = if (isLight) Color(0x113B82F6) else Color(0x15FFFFFF),
                                fontSize = 96.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-8).dp, y = (-36).dp)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Inspiration Star",
                                        tint = if (isLight) SamsungBlue else OrbitViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DAILY MOTIVATION",
                                        color = if (isLight) SamsungBlue else OrbitViolet,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Text(
                                    text = randomQuote,
                                    color = themeTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tap quote to shuffle mindset",
                                        color = themeTextTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val context = LocalContext.current
                                        IconButton(
                                            onClick = {
                                                val shareIntent = android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "“$randomQuote” — Shared from Consistency Hub")
                                                    type = "text/plain"
                                                }
                                                try {
                                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Daily Motivation"))
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(context, "No app available to share", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share Quote",
                                                tint = if (isLight) SamsungBlue else OrbitViolet,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Shuffle Quote",
                                            tint = if (isLight) SamsungBlue else OrbitViolet,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fluid Tab Switcher Including Profile Section (One UI Segmented Layout)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isLight) Color(0xFFF1F5F9) else Color(0x1F252528))
                            .border(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x0EFFFFFF), RoundedCornerShape(18.dp))
                            .padding(4.dp)
                            .testTag("dashboard_tab_switcher")
                    ) {
                        DashboardTab.entries.forEach { tab ->
                            val isActive = currentTab == tab

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .run {
                                        if (isActive && isLight) {
                                            this.shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), clip = false)
                                        } else {
                                            this
                                        }
                                    }
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isActive) (if (isLight) Color.White else Color(0x26FFFFFF)) else Color.Transparent)
                                    .bounceClickable {
                                        viewModel.selectedTab.value = tab
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (tab) {
                                        DashboardTab.Tasks -> "Tasks"
                                        DashboardTab.Notes -> "Vault"
                                        DashboardTab.Metrics -> "Metrics"
                                        DashboardTab.Profile -> "Profile"
                                    },
                                    color = if (isActive) (if (isLight) Color.Black else Color.White) else themeTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                    },
                    label = "WorkspaceTabAnimation"
                ) { tab ->
                    when (tab) {
                        DashboardTab.Tasks -> TaskTrackerTab(viewModel, isLight)
                        DashboardTab.Notes -> NotesTab(viewModel, isLight)
                        DashboardTab.Metrics -> MetricTrackerTab(viewModel, isLight)
                        DashboardTab.Profile -> ProfileTab(viewModel, isLight)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: PROFILE STATUS (SAMSUNG ONE UI DETAILS)
// ==========================================

@Composable
fun ProfileTab(viewModel: ConsistencyHubViewModel, isLight: Boolean) {
    val activeUser by viewModel.currentUsername.collectAsStateWithLifecycle()
    val activeUserFullName by viewModel.currentUserFullName.collectAsStateWithLifecycle()
    
    val oneUiVer by viewModel.oneUiVersion.collectAsStateWithLifecycle()
    val galaxyAiAnalysis by viewModel.galaxyAiAnalysis.collectAsStateWithLifecycle()
    val isGalaxyAiProcessing by viewModel.isGalaxyAiProcessing.collectAsStateWithLifecycle()
    
    var isAiHubExpanded by remember { mutableStateOf(false) }
    
    val displayName = activeUserFullName ?: "Premchand Gunturu"
    val initials = displayName.split(" ")
        .filter { it.isNotEmpty() }
        .map { it[0] }
        .joinToString("")
        .take(2)
        .uppercase()

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Samsung-style Profile Avatar
        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SamsungBlue, OrbitViolet)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (initials.isNotEmpty()) initials else "PG",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = displayName,
            color = themeTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${activeUser ?: "study_grinder"}",
            color = themeTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Details Card (One UI Premium Look)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_details_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeCardBg),
            border = BorderStroke(1.dp, themeBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileDetailItem("Username Identifier", "@${activeUser ?: "study_grinder"}", Icons.Default.AccountCircle, themeTextPrimary, themeTextSecondary)
                HorizontalDivider(color = themeBorder.copy(alpha = 0.5f))
                ProfileDetailItem("Workspace Role", "Consistency Elite Pro", Icons.Default.Verified, themeTextPrimary, themeTextSecondary)
                HorizontalDivider(color = themeBorder.copy(alpha = 0.5f))
                ProfileDetailItem("Active Streak", "7 Days Streak", Icons.Default.Stars, themeTextPrimary, themeTextSecondary)
                HorizontalDivider(color = themeBorder.copy(alpha = 0.5f))
                ProfileDetailItem("Device Security", "Biometric Local Sync", Icons.Default.Security, themeTextPrimary, themeTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Samsung One UI 8.5 Interactive Engine Card in Profile
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("oneui_8_5_control_hub"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF111622)
            ),
            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x11FFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAiHubExpanded = !isAiHubExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Galaxy AI Active Icon",
                            tint = SamsungBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Galaxy AI & One UI 8.5 Hub",
                            color = themeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLight) Color(0x333B82F6) else Color(0x1F3B82F6))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v$oneUiVer Active",
                            color = SamsungBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (isAiHubExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = if (isLight) Color(0xFFE2E8F0) else Color(0x1F252528))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Choice 1: Samsung UI Version Selector
                    Text(
                        text = "SAMSUNG ONE UI INTERACTIVE VERSION",
                        color = themeTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val versions = listOf("8.5", "8.0", "7.1")
                        versions.forEach { ver ->
                            val isSel = oneUiVer == ver
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) SamsungBlue else (if (isLight) Color(0xFFF1F5F9) else Color(0xFF1B2230)))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) SamsungBlue else (if (isLight) Color(0xFFE2E8F0) else Color(0x1F252528)),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.oneUiVersion.value = ver }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic radius adapting to selected One UI version
                                val labelText = when(ver) {
                                    "8.5" -> "One UI 8.5"
                                    "8.0" -> "One UI 8.0"
                                    else -> "One UI 7.1"
                                }
                                Text(
                                    text = labelText,
                                    color = if (isSel) Color.White else themeTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Choice 2: Galaxy AI Productivity Audit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GALAXY AI STUDY ENGINE",
                                color = themeTextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Review study goals and notes instantly",
                                color = themeTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.triggerGalaxyAi() },
                            enabled = !isGalaxyAiProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SamsungBlue,
                                contentColor = Color.White,
                                disabledContainerColor = SamsungBlue.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGalaxyAiProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Audit Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    galaxyAiAnalysis?.let { analysisText ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isLight) Color(0xFFF8FAFC) else Color(0xFF1E2638))
                                .border(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Galaxy Secure Advisor",
                                        tint = ActiveEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Galaxy Intelligence Advisory",
                                        color = ActiveEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysisText,
                                    color = themeTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Accent Logout Button at the bottom
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("logout_button_profile"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444) // One UI red warning
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Log out",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Exit Active Session",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Created by Premchand",
            color = themeTextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileDetailItem(
    label: String,
    value: String,
    icon: ImageVector,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = SamsungBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// SCREEN 1: TASK TRACKER (FOCUS LIST)
// ==========================================

@Composable
fun TaskTrackerTab(viewModel: ConsistencyHubViewModel, isLight: Boolean) {
    val tasksList by viewModel.tasks.collectAsStateWithLifecycle()
    val taskTitle by viewModel.taskTitleInput.collectAsStateWithLifecycle()
    val currentPreset by viewModel.selectedSubjectPreset.collectAsStateWithLifecycle()
    val customTagVal by viewModel.customTagInput.collectAsStateWithLifecycle()

    var showAlertDialogForTask by remember { mutableStateOf<Task?>(null) }

    // Dialog state variables (declared at top Tab-level for absolute compose safety)
    var dayOffset by remember { mutableStateOf(0) }
    var hourSelected by remember { mutableStateOf(12) }
    var minuteSelected by remember { mutableStateOf(0) }

    LaunchedEffect(showAlertDialogForTask) {
        if (showAlertDialogForTask != null) {
            dayOffset = 0
            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }
            hourSelected = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            minuteSelected = (m / 5) * 5
        }
    }

    val targetCalendar = remember(dayOffset, hourSelected, minuteSelected) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, hourSelected)
            set(Calendar.MINUTE, minuteSelected)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val targetFormatted = remember(targetCalendar) {
        SimpleDateFormat("EEE, MMM dd 'at' HH:mm", Locale.getDefault()).format(targetCalendar.time)
    }

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val themeInputFocusedBg = if (isLight) Color(0xFFF1F5F9) else Color(0x33000000)
    val themeInputUnfocusedBg = if (isLight) Color(0xFFF8FAFC) else Color(0x1A000000)

    // Completion math
    val totalTasks = tasksList.size
    val completedTasksCount = tasksList.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("task_scroller"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Visual Completion Stats Card
        item {
            GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_stats_card"),
            isLight = isLight
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Deck Velocity",
                        color = themeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedTasksCount of $totalTasks goals achieved",
                        color = themeTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Dial/Circular Indicator mapping completed percent
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (totalTasks > 0) completedTasksCount.toFloat() / totalTasks.toFloat() else 0f
                    val sweepAnim by animateFloatAsState(targetValue = progress * 360f, label = "gaugeSweep")

                    Canvas(modifier = Modifier.size(50.dp)) {
                        // Background track
                        drawArc(
                            color = themeBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
                        )
                        // Progress sweep
                        drawArc(
                            color = SamsungBlue,
                            startAngle = -90f,
                            sweepAngle = sweepAnim,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.drawscope.Stroke.DefaultCap
                            )
                        )
                    }

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = themeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Focus Add Controls Tray (moved to top of focus screen)
        item {
            Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("add_task_tray"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = themeCardBg
            ),
            border = BorderStroke(1.dp, themeBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { viewModel.taskTitleInput.value = it },
                        placeholder = { Text("What study target now?") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeTextPrimary,
                            unfocusedTextColor = themeTextPrimary,
                            focusedBorderColor = SamsungBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = themeInputFocusedBg,
                            unfocusedContainerColor = themeInputUnfocusedBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_task_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.addNewTask() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SamsungBlue)
                            .testTag("task_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Submit Target",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject presets scroll row
                val subjectList = listOf("Admin", "Algorithm", "Formula", "Meeting", "Chore")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectList.forEach { category ->
                        val selected = currentPreset == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) SamsungBlue else (if (isLight) Color(0xFFE2E8F0) else Color(0xFF141923)))
                                .border(0.5.dp, if (selected) SamsungBlue else themeBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectedSubjectPreset.value = category }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("preset_pill_$category")
                        ) {
                            Text(
                                text = category,
                                color = if (selected) Color.White else themeTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Tag input
                OutlinedTextField(
                    value = customTagVal,
                    onValueChange = { viewModel.customTagInput.value = it },
                    placeholder = { Text("Or craft custom subcategory tag (e.g. #math)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeTextPrimary,
                        unfocusedTextColor = themeTextPrimary,
                        focusedBorderColor = SamsungBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = themeInputFocusedBg,
                        unfocusedContainerColor = themeInputUnfocusedBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_tag_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }
    }

    item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (tasksList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Zero objectives",
                            tint = themeTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No procrastination targets active.",
                            color = themeTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(tasksList, key = { it.id }) { task ->
                    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    val cardAlpha by animateFloatAsState(targetValue = if (task.isCompleted) 0.5f else 1f, label = "cardAlpha")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = cardAlpha }
                            .testTag("task_item_${task.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) (if (isLight) Color(0x06000000) else Color(0x08FFFFFF)) else themeCardBg
                        ),
                        border = BorderStroke(1.dp, if (task.isCompleted) Color.Transparent else themeBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Target Invalidator checkbox (fluid bouncy click)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .bounceClickable { viewModel.toggleTask(task) }
                                    .testTag("task_check_${task.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete task",
                                    tint = if (task.isCompleted) ActiveEmerald else SamsungBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = if (task.isCompleted) themeTextSecondary else themeTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = textDecoration
                                )

                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom visual Tags
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (task.subject) {
                                                    "Admin" -> (if (isLight) Color(0xFFFEE2E2) else PastelRed.copy(alpha = 0.2f))
                                                    "Algorithm" -> (if (isLight) Color(0xFFDBEAFE) else PastelBlue.copy(alpha = 0.2f))
                                                    "Formula" -> (if (isLight) Color(0xFFD1FAE5) else PastelGreen.copy(alpha = 0.2f))
                                                    "Meeting" -> (if (isLight) Color(0xFFFEF3C7) else PastelYellow.copy(alpha = 0.2f))
                                                    "Chore" -> (if (isLight) Color(0xFFF3E8FF) else PastelPurple.copy(alpha = 0.2f))
                                                    else -> (if (isLight) Color(0xFFE0F2FE) else SamsungBlue.copy(alpha = 0.15f))
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = task.subject,
                                            color = when (task.subject) {
                                                "Admin" -> (if (isLight) Color(0xFFDC2626) else PastelRed)
                                                "Algorithm" -> (if (isLight) Color(0xFF2563EB) else PastelBlue)
                                                "Formula" -> (if (isLight) Color(0xFF16A34A) else PastelGreen)
                                                "Meeting" -> (if (isLight) Color(0xFFD97706) else PastelYellow)
                                                "Chore" -> (if (isLight) Color(0xFF9333EA) else PastelPurple)
                                                else -> (if (isLight) SamsungBlue else SamsungBlue)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    task.alertTime?.let { alertEpoch ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Scheduled Alarm",
                                            tint = if (isLight) Color(0xFFD97706) else PastelYellow,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        Text(
                                            text = formatter.format(Date(alertEpoch)),
                                            color = if (isLight) Color(0xFFD97706) else PastelYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Alert Alarm Icon trigger
                            IconButton(
                                onClick = { showAlertDialogForTask = task },
                                modifier = Modifier
                                    .testTag("task_alert_trigger_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Slated Alerts",
                                    tint = if (task.alertTime != null) (if (isLight) Color(0xFFD97706) else PastelYellow) else themeTextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete objective button
                            IconButton(
                                onClick = { viewModel.deleteTask(task.id) },
                                modifier = Modifier
                                    .testTag("task_delete_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sweep item",
                                    tint = themeTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal popup option sheets to schedule target future alert times
    if (showAlertDialogForTask != null) {
        val targetTask = showAlertDialogForTask!!
        
        Dialog(onDismissRequest = { showAlertDialogForTask = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLight) Color.White else DarkSlate
                ),
                border = BorderStroke(1.dp, themeBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Schedule Focus Reminder",
                        color = themeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Anchor alert notifications for: ${targetTask.title}",
                        color = themeTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // CUSTOM DATE & TIME PICKER SECTION
                    Text(
                        text = "CHOOSE EXACT REMINDER DATE",
                        color = themeTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("Today", 0),
                            Pair("Tomorrow", 1),
                            Pair("In 2 Days", 2),
                            Pair("In 3 Days", 3)
                        ).forEach { (dayLabel, offset) ->
                            val isSel = dayOffset == offset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) SamsungBlue else (if (isLight) Color(0xFFF1F5F9) else Color(0xFF1B2230)))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) SamsungBlue else (if (isLight) Color(0xFFE2E8F0) else Color(0x1F252528)),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { dayOffset = offset }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    color = if (isSel) Color.White else themeTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Stepper
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "HOUR",
                                color = themeTextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33374151))
                                        .clickable {
                                            hourSelected = (hourSelected + 23) % 24
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = themeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Text(
                                    text = String.format("%02d", hourSelected),
                                    color = themeTextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33374151))
                                        .clickable {
                                            hourSelected = (hourSelected + 1) % 24
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = themeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Text(
                            text = ":",
                            color = themeTextSecondary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        
                        // Minute Stepper
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MINUTE",
                                color = themeTextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33374151))
                                        .clickable {
                                            minuteSelected = (minuteSelected + 55) % 60
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = themeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Text(
                                    text = String.format("%02d", minuteSelected),
                                    color = themeTextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33374151))
                                        .clickable {
                                            minuteSelected = (minuteSelected + 5) % 60
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = themeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = "ALERT TARGET: $targetFormatted",
                        color = SamsungBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.scheduleTaskAlertExact(targetTask, targetCalendar.timeInMillis)
                            showAlertDialogForTask = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SamsungBlue
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Confirm Custom Alarm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = themeBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // QUICK DELAY PRESETS
                    Text(
                        text = "QUICK ALARM PRESETS",
                        color = themeTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                    )

                    val delayOptions = listOf(
                        Pair("In 5 mins", 5),
                        Pair("In 15 mins", 15),
                        Pair("In 1 hour", 60)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        delayOptions.forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLight) Color(0xFFF8FAFC) else Color(0x331F2937))
                                    .border(0.5.dp, themeBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.scheduleTaskAlert(targetTask, opt.second)
                                        showAlertDialogForTask = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = opt.first, color = SamsungBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { showAlertDialogForTask = null }) {
                        Text(text = "Dismiss", color = if (isLight) Color(0xFFEF4444) else PastelRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: THOUGHT VAULT (KNOWLEDGE ARCHIVE)
// ==========================================

@Composable
fun NotesTab(viewModel: ConsistencyHubViewModel, isLight: Boolean) {
    val displayedThoughts by viewModel.filteredThoughts.collectAsStateWithLifecycle()
    val rawSearch by viewModel.thoughtSearchQuery.collectAsStateWithLifecycle()

    var showDepositDialog by remember { mutableStateOf(false) }

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val themeInputFocusedBg = if (isLight) Color(0xFFF1F5F9) else Color(0x33000000)
    val themeInputUnfocusedBg = if (isLight) Color(0xFFF8FAFC) else Color(0x1A000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Fluid Filter Search Bar
        OutlinedTextField(
            value = rawSearch,
            onValueChange = { viewModel.thoughtSearchQuery.value = it },
            placeholder = { Text("Query formula, code block, or archived thought...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Query") },
            trailingIcon = {
                if (rawSearch.isNotEmpty()) {
                    IconButton(onClick = { viewModel.thoughtSearchQuery.value = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear query", tint = themeTextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = themeTextPrimary,
                unfocusedTextColor = themeTextPrimary,
                focusedBorderColor = OrbitViolet,
                unfocusedBorderColor = themeBorder,
                focusedContainerColor = themeInputFocusedBg,
                unfocusedContainerColor = themeInputUnfocusedBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("thought_search_bar"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Secure Knowledge Grid",
                color = themeTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Add Thought float/trigger
            IconButton(
                onClick = { showDepositDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrbitViolet)
                    .testTag("trigger_thought_dialog")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Secure Deposit", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Two-column Masonry structured grid for thoughts layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .testTag("thoughts_grid"),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayedThoughts.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Zero notes",
                            tint = themeTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (rawSearch.isNotEmpty()) "No results found." else "Vault empty. Deposit brainstorming.",
                            color = themeTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(displayedThoughts, key = { it.id }) { thought ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("thought_card_${thought.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = themeCardBg
                        ),
                        border = BorderStroke(1.dp, themeBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = thought.description,
                                color = themeTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Clickable extracted hashtags
                            val hashes = viewModel.extractHashtags(thought.tags)
                            if (hashes.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.wrapContentSize().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    hashes.forEach { hashtag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isLight) Color(0xFFF1F5F9) else Color(0xFF13111C))
                                                .border(0.5.dp, OrbitViolet.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .clickable {
                                                    // Clicking the hashtag auto-fills search
                                                    viewModel.thoughtSearchQuery.value = hashtag.removePrefix("#")
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = hashtag,
                                                color = OrbitViolet,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm", Locale.getDefault()).format(Date(thought.timestamp)),
                                    color = themeTextTertiary,
                                    fontSize = 9.sp
                                )

                                IconButton(
                                    onClick = { viewModel.deleteThought(thought.id) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("thought_delete_${thought.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Flush thought",
                                        tint = themeTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Secure Deposit Dialog Modal
    if (showDepositDialog) {
        val entryDesc by viewModel.thoughtDescriptionInput.collectAsStateWithLifecycle()
        val entryTags by viewModel.thoughtTagsInput.collectAsStateWithLifecycle()

        Dialog(onDismissRequest = { showDepositDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deposit_thought_dialog"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLight) Color.White else DarkSlate
                ),
                border = BorderStroke(1.dp, themeBorder)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Deposit Thought Archive",
                        color = themeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Write your brainstorming or study notes below securely stored in your local SQL layer.",
                        color = themeTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                    )

                    // Textarea prompt
                    OutlinedTextField(
                        value = entryDesc,
                        onValueChange = { viewModel.thoughtDescriptionInput.value = it },
                        placeholder = { Text("Describe the logic block, code, formula or daily summary here...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeTextPrimary,
                            unfocusedTextColor = themeTextPrimary,
                            focusedBorderColor = OrbitViolet,
                            unfocusedBorderColor = themeBorder,
                            focusedContainerColor = themeInputFocusedBg,
                            unfocusedContainerColor = themeInputUnfocusedBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("thought_entry_input"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comma tags prompt
                    OutlinedTextField(
                        value = entryTags,
                        onValueChange = { viewModel.thoughtTagsInput.value = it },
                        placeholder = { Text("Tags as raw comma list (e.g. math, code, lock)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeTextPrimary,
                            unfocusedTextColor = themeTextPrimary,
                            focusedBorderColor = OrbitViolet,
                            unfocusedBorderColor = themeBorder,
                            focusedContainerColor = themeInputFocusedBg,
                            unfocusedContainerColor = themeInputUnfocusedBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("thought_tags_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDepositDialog = false }) {
                            Text(text = "Cancel", color = if (isLight) Color(0xFFEF4444) else PastelRed)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.addNewThought()
                                showDepositDialog = false
                            },
                            modifier = Modifier.testTag("submit_thought_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrbitViolet
                            )
                        ) {
                            Text(text = "Archive Thought", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: CONSISTENCY HUB (METRIC LOGGER)
// ==========================================

@Composable
fun MetricTrackerTab(viewModel: ConsistencyHubViewModel, isLight: Boolean) {
    val loggedList by viewModel.studyLogs.collectAsStateWithLifecycle()
    val inputHours by viewModel.studyHoursInput.collectAsStateWithLifecycle()
    val inputTopic by viewModel.studyTopicInput.collectAsStateWithLifecycle()

    val weeklyMetrics = viewModel.calculateWeeklyEffortMetrics(loggedList)
    val totalWeeklyHours = weeklyMetrics.sumOf { it.hoursLogged }
    val recommendedWeeklyHours = 70.0 // 10 hrs per day scale

    val themeTextPrimary = if (isLight) Color(0xFF111827) else TextPrimary
    val themeTextSecondary = if (isLight) Color(0xFF4B5563) else TextSecondary
    val themeTextTertiary = if (isLight) Color(0xFF9CA3AF) else TextTertiary
    val themeBorder = if (isLight) Color(0xFFE2E8F0) else GlassBorder
    val themeCardBg = if (isLight) Color(0xFFFFFFFF) else GlassCardBackground
    val themeInputFocusedBg = if (isLight) Color(0xFFF1F5F9) else Color(0x33000000)
    val themeInputUnfocusedBg = if (isLight) Color(0xFFF8FAFC) else Color(0x1A000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 7-Day Goal Tracker Canvas & Progress Bars
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("7_day_goal_card"),
            isLight = isLight
        ) {
            Text(
                text = "7-Day Goal Tracker",
                color = themeTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Compounded Effort: ",
                    color = themeTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = String.format("%.1f hrs", totalWeeklyHours),
                    color = ActiveEmerald,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " accumulated of $recommendedWeeklyHours hours",
                    color = themeTextSecondary,
                    fontSize = 13.sp
                )
            }

            // Visual graph row showing Monday-Sunday logs relative to 10-hour daily goals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(top = 10.dp)
                    .testTag("weekly_bars_grid"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyMetrics.forEach { metric ->
                    val dailyTarget = 10.0
                    val ratio = (metric.hoursLogged / dailyTarget).coerceIn(0.0, 1.0).toFloat()
                    val barHeightAnim by animateFloatAsState(targetValue = ratio, label = "barHeight")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label at the top of active bar
                        if (metric.hoursLogged > 0.0) {
                            Text(
                                text = String.format(Locale.US, "%.1f", metric.hoursLogged),
                                color = ActiveEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        } else {
                            Text(text = "0", color = themeTextTertiary, fontSize = 9.sp, modifier = Modifier.padding(bottom = 2.dp))
                        }

                        // Colored pillar
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .weight(1f, fill = false)
                                .fillMaxHeight(barHeightAnim.coerceAtLeast(0.06f))
                                .clip(CircleShape)
                                .background(
                                    if (metric.hoursLogged > 0.0) SamsungBlue else (if (isLight) Color(0xFFCBD5E1) else Color(0x33FFFFFF))
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = metric.dayName,
                            color = if (metric.hoursLogged > 0.0) SamsungBlue else (if (isLight) Color(0xFF94A3B8) else Color(0xFF64748B)),
                            fontSize = 11.sp,
                            fontWeight = if (metric.hoursLogged > 0.0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register Study Log Form
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_log_form"),
            isLight = isLight
        ) {
            Text(
                text = "Log Dynamic Study Effort",
                color = themeTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Duration input
            OutlinedTextField(
                value = inputHours,
                onValueChange = { viewModel.studyHoursInput.value = it },
                label = { Text("Log Duration hours (e.g. 3.5)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeTextPrimary,
                    unfocusedTextColor = themeTextPrimary,
                    focusedBorderColor = ActiveEmerald,
                    unfocusedBorderColor = themeBorder,
                    focusedContainerColor = themeInputFocusedBg,
                    unfocusedContainerColor = themeInputUnfocusedBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_hours_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Topic input
            OutlinedTextField(
                value = inputTopic,
                onValueChange = { viewModel.studyTopicInput.value = it },
                label = { Text("Topic researched (e.g. Cryptography)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeTextPrimary,
                    unfocusedTextColor = themeTextPrimary,
                    focusedBorderColor = ActiveEmerald,
                    unfocusedBorderColor = themeBorder,
                    focusedContainerColor = themeInputFocusedBg,
                    unfocusedContainerColor = themeInputUnfocusedBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_topic_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.addNewStudyLog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("log_effort_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveEmerald
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Submit Activity Hour Log", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chronological Study History Roll
        Text(
            text = "Activity Log Archive",
            color = themeTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Stacking our list logs in vertical sequence
        if (loggedList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = "Zero activity logs",
                    tint = themeTextTertiary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "No recorded efforts logged yet.", color = themeTextSecondary, fontSize = 13.sp)
            }
        } else {
            loggedList.forEach { log ->
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(bottom = 8.dp)
                         .bounceClickable { }
                         .testTag("log_item_${log.id}"),
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(
                         containerColor = themeCardBg
                     ),
                     border = BorderStroke(1.dp, themeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ActiveEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1fh", log.durationHours),
                                    color = ActiveEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = log.topic,
                                    color = themeTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm - dd MMM yyyy", Locale.getDefault()).format(Date(log.timestamp)),
                                    color = themeTextTertiary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteStudyLog(log.id) },
                            modifier = Modifier
                                .testTag("log_delete_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry log",
                                tint = themeTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
