# Complete Guide: Fixing Bottom Bar Jump Issues with SOLID Architecture

## Problem Statement

When navigating from a tab-based screen to a detail screen in Compose, the bottom navigation bar disappears, causing the Scaffold's `innerPadding` to change abruptly. This creates a visual glitch where content jumps before the transition animation completes.

## Root Cause Analysis

```kotlin
// ❌ PROBLEMATIC PATTERN
Scaffold(
    bottomBar = { 
        if (showBottomBar) NavigationBar { ... } 
    }
) { padding ->
    AnimatedContent(destination) { target ->
        when (target) {
            TabDestination -> TabContent(modifier = Modifier.padding(padding))
            DetailDestination -> DetailContent(modifier = Modifier.padding(padding))
        }
    }
}
```

**Why this fails:**
1. When `showBottomBar` changes from `true` → `false`, the padding changes immediately
2. The content inside `AnimatedContent` receives new padding mid-animation
3. The layout shifts vertically before the slide animation finishes
4. Result: Jarring visual jump

## Solution Overview

**Key Insight:** Make detail screens **siblings** to the tab layout, not **children** of the same Scaffold.

```
❌ BAD (children):          ✅ GOOD (siblings):
    Scaffold                    AnimatedContent
    ├─ BottomBar                ├─ TabLayout
    └─ Content                  │  └─ Scaffold + BottomBar
       └─ AnimatedContent       └─ DetailLayout
          ├─ Tabs                  └─ Full screen
          └─ Details
```

---

## Step-by-Step Implementation Guide

### Phase 1: Create Type-Safe Destination Hierarchy

#### Step 1.1: Refactor Destination Model

**File:** Your navigation model file (e.g., `AppNavigationModels.kt`)

**Before:**
```kotlin
sealed class HomeDestination {
    data object ConversationList : HomeDestination()
    data object Settings : HomeDestination()
    data object Contacts : HomeDestination()
    data class Detail(val id: String) : HomeDestination()
}
```

**After:**
```kotlin
/**
 * Navigation destinations categorized by UI context.
 * 
 * - TabDestination: Shows within tab navigation with bottom bar
 * - DetailDestination: Full-screen overlays without bottom bar
 * 
 * Follows Open/Closed Principle - new destinations extend sealed classes
 * without modifying navigation logic.
 */
sealed class HomeDestination {
    /**
     * Tab destinations with intrinsic animation order.
     * animationOrder determines slide direction (lower → higher slides right).
     */
    sealed class TabDestination(val animationOrder: Int) : HomeDestination() {
        data object ConversationList : TabDestination(animationOrder = 0)
        data object Calls : TabDestination(animationOrder = 1)
        data object Settings : TabDestination(animationOrder = 2)
        // Add more tabs here with sequential animationOrder
    }

    /**
     * Full-screen detail destinations.
     */
    sealed class DetailDestination : HomeDestination() {
        data object Contacts : DetailDestination()
        
        data class ConversationDetail(
            val conversationId: String,
            val contactName: String? = null,
        ) : DetailDestination()
        // Add more details here
    }
}
```

**Why this matters:**
- ✅ Type system enforces categorization at compile-time
- ✅ No need for `isDetailDestination()` helper functions (OCP violation)
- ✅ Each destination owns its animation behavior
- ✅ New destinations don't require modifying existing code

#### Step 1.2: Update State Mapping

**File:** Your UI state model (e.g., `AppUiState.kt`)

**Before:**
```kotlin
data class HomeUiState(...) {
    val destination: HomeDestination
        get() = when {
            activeDetailId != null -> HomeDestination.Detail(activeDetailId)
            isContactsVisible -> HomeDestination.Contacts
            selectedTab == Tab.Settings -> HomeDestination.Settings
            else -> HomeDestination.ConversationList
        }
}
```

**After:**
```kotlin
data class HomeUiState(...) {
    val destination: HomeDestination
        get() = when {
            activeDetailId != null -> HomeDestination.DetailDestination.ConversationDetail(activeDetailId)
            isContactsVisible -> HomeDestination.DetailDestination.Contacts
            selectedTab == Tab.Settings -> HomeDestination.TabDestination.Settings
            else -> HomeDestination.TabDestination.ConversationList
        }
}
```

---

### Phase 2: Create Layout Mode Abstraction

#### Step 2.1: Define HomeLayout Sealed Interface

**File:** Your main screen file (e.g., `HomeScreen.kt`)

Add this **at the top of the file**, before your `@Composable` functions:

```kotlin
/**
 * High-level layout mode for the Home screen.
 * 
 * - [Tabs]: Tab-based navigation with bottom bar (Scaffold manages padding)
 * - [Detail]: Full-screen overlay without bottom bar (manual padding)
 */
private sealed interface HomeLayout {
    data class Tabs(val destination: HomeDestination.TabDestination) : HomeLayout
    data class Detail(val destination: HomeDestination.DetailDestination) : HomeLayout
}
```

**Why this matters:**
- ✅ Explicit representation of rendering context
- ✅ Makes animation logic clearer
- ✅ Enables pattern matching for layout switching

---

### Phase 3: Refactor Main Screen Composable

#### Step 3.1: Convert Destination to Layout Mode

**At the start of your main composable:**

```kotlin
@Composable
fun HomeScreen(
    state: HomeUiState,
    // ... other parameters
) {
    val strings = liveChatStrings() // or your strings resource
    val destination = state.destination
    val reduceMotion = LocalReduceMotion.current
    
    // Convert destination to layout mode
    val layout: HomeLayout = remember(destination) {
        when (destination) {
            is HomeDestination.TabDestination -> HomeLayout.Tabs(destination)
            is HomeDestination.DetailDestination -> HomeLayout.Detail(destination)
        }
    }
    
    // ... rest of implementation
}
```

#### Step 3.2: Handle Back Gestures

```kotlin
    // Back gesture handling for detail destinations
    val backGestureEnabled = layout is HomeLayout.Detail
    val backGestureAction = remember(layout) {
        when (val detail = (layout as? HomeLayout.Detail)?.destination) {
            is HomeDestination.DetailDestination.ConversationDetail -> onBackFromConversation
            HomeDestination.DetailDestination.Contacts -> onCloseContacts
            null -> { {} }
        }
    }
    
    PlatformBackGestureHandler(
        enabled = backGestureEnabled,
        onBack = rememberStableAction(backGestureAction),
    )
```

#### Step 3.3: Setup Notification State (if applicable)

```kotlin
    // In-app notification state
    var currentNotification by remember { mutableStateOf<InAppNotification?>(null) }
    var showNotification by remember { mutableStateOf(false) }
    var dismissJob by remember { mutableStateOf<Job?>(null) }
    
    // Use rememberUpdatedState to avoid effect restarts
    val currentDestination by rememberUpdatedState(destination)
    
    LaunchedEffect(Unit) {
        YourNotificationCenter.events.collect { notification ->
            // Don't show if viewing that content
            if (currentDestination is HomeDestination.DetailDestination.ConversationDetail && 
                currentDestination.conversationId == notification.conversationId) {
                return@collect
            }
            
            dismissJob?.cancel()
            currentNotification = notification
            showNotification = true
            
            dismissJob = launch {
                delay(5000)
                showNotification = false
                delay(300) // Animation duration
                currentNotification = null
            }
        }
    }
```

#### Step 3.4: Root Box with AnimatedContent

**The key pattern - replace your Scaffold with this:**

```kotlin
    Box(modifier = modifier.fillMaxSize()) {
        // Main content with layout switching
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = layout,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith 
                        fadeOut(animationSpec = tween(100))
                } else {
                    val isEnteringDetail = targetState is HomeLayout.Detail
                    val isExitingDetail = initialState is HomeLayout.Detail
                    
                    when {
                        isEnteringDetail -> {
                            // Slide in from right when entering detail
                            (
                                slideInHorizontally(
                                    animationSpec = tween(300),
                                ) { fullWidth -> fullWidth / 3 } + 
                                fadeIn(animationSpec = tween(300))
                            ) togetherWith
                                fadeOut(animationSpec = tween(200))
                        }
                        isExitingDetail -> {
                            // Slide out to right when exiting detail
                            fadeIn(animationSpec = tween(300)) togetherWith
                                (
                                    slideOutHorizontally(
                                        animationSpec = tween(300),
                                    ) { fullWidth -> fullWidth / 3 } + 
                                    fadeOut(animationSpec = tween(200))
                                )
                        }
                        else -> {
                            // Fallback (shouldn't happen)
                            fadeIn(animationSpec = tween(200)) togetherWith 
                                fadeOut(animationSpec = tween(200))
                        }
                    }
                }
            },
            label = "HomeLayoutTransition",
        ) { currentLayout ->
            when (currentLayout) {
                is HomeLayout.Tabs -> {
                    HomeTabsLayout(
                        modifier = Modifier.fillMaxSize(),
                        tabDestination = currentLayout.destination,
                        // ... pass necessary callbacks
                    )
                }
                is HomeLayout.Detail -> {
                    HomeDetailLayout(
                        modifier = Modifier.fillMaxSize(),
                        detailDestination = currentLayout.destination,
                        // ... pass necessary callbacks
                    )
                }
            }
        }
        
        // Optional: Overlay elements (notifications, dialogs, etc.)
        // Place them here so they work across both layouts
    }
```

---

### Phase 4: Create Tab Layout Composable

#### Step 4.1: Extract HomeTabsLayout

Create a **private** composable in the same file:

```kotlin
/**
 * Layout for tab-based navigation with bottom bar.
 * This composable owns the Scaffold and manages stable padding.
 */
@Composable
private fun HomeTabsLayout(
    tabDestination: HomeDestination.TabDestination,
    selectedTab: YourTabEnum,
    onSelectTab: (YourTabEnum) -> Unit,
    // ... other callbacks
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    
    // Tab configuration (icons, labels)
    val tabs = remember { defaultHomeTabs }
    val tabOptions = remember(strings.home, tabs) {
        tabs.map { tabItem ->
            HomeTabOption(
                tab = tabItem.tab,
                label = tabItem.labelSelector(strings.home),
                icon = tabItem.icon,
            )
        }
    }
    
    // Optional: Dynamic chrome visibility (e.g., Settings submenus)
    var chromeVisibility by remember {
        mutableStateOf(
            ChromeVisibility(
                showTopBar = true,
                showBottomBar = true,
            ),
        )
    }
    val onChromeVisibilityChanged = remember {
        { chrome: ChromeVisibility -> chromeVisibility = chrome }
    }
    
    // Compute bottom bar visibility
    val showBottomBar = remember(tabDestination, chromeVisibility.showBottomBar) {
        tabDestination != HomeDestination.TabDestination.Settings || 
            chromeVisibility.showBottomBar
    }
    
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    tabOptions.forEach { tabItem ->
                        val onTabClick = remember(tabItem.tab, onSelectTab) {
                            { onSelectTab(tabItem.tab) }
                        }
                        NavigationBarItem(
                            selected = selectedTab == tabItem.tab,
                            onClick = onTabClick,
                            icon = { Icon(tabItem.icon, contentDescription = tabItem.label) },
                            label = { Text(tabItem.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        // Stabilize modifier to reduce recompositions
        val contentModifier = remember(padding, showBottomBar) {
            Modifier
                .padding(padding)
                .fillMaxSize()
                .then(
                    if (showBottomBar) {
                        Modifier
                    } else {
                        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    },
                )
        }
        
        // Animate between tab destinations
        AnimatedContent(
            modifier = contentModifier,
            targetState = tabDestination,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith 
                        fadeOut(animationSpec = tween(100))
                } else {
                    // Use intrinsic animationOrder property
                    val direction = when {
                        targetState.animationOrder > initialState.animationOrder -> 1
                        targetState.animationOrder < initialState.animationOrder -> -1
                        else -> 0
                    }
                    
                    if (direction == 0) {
                        fadeIn(animationSpec = tween(200)) togetherWith 
                            fadeOut(animationSpec = tween(200))
                    } else {
                        (
                            slideInHorizontally(
                                animationSpec = tween(300),
                            ) { fullWidth -> fullWidth / 4 * direction } + 
                            fadeIn(animationSpec = tween(300))
                        ) togetherWith
                            (
                                slideOutHorizontally(
                                    animationSpec = tween(300),
                                ) { fullWidth -> -fullWidth / 4 * direction } + 
                                fadeOut(animationSpec = tween(200))
                            )
                    }
                }
            },
            label = "TabTransition",
        ) { target ->
            when (target) {
                HomeDestination.TabDestination.ConversationList ->
                    ConversationListRoute(
                        modifier = Modifier.fillMaxSize(),
                        // ... callbacks
                    )
                
                HomeDestination.TabDestination.Calls ->
                    CallsRoute(
                        modifier = Modifier.fillMaxSize(),
                    )
                
                HomeDestination.TabDestination.Settings ->
                    SettingsRoute(
                        modifier = Modifier.fillMaxSize(),
                        onChromeVisibilityChanged = onChromeVisibilityChanged,
                        // ... callbacks
                    )
                
                // Add your other tab destinations here
            }
        }
    }
}

// Helper data class for tab configuration
private data class HomeTabOption(
    val tab: YourTabEnum,
    val label: String,
    val icon: ImageVector,
)
```

---

### Phase 5: Create Detail Layout Composable

#### Step 5.1: Extract HomeDetailLayout

```kotlin
/**
 * Layout for full-screen detail destinations.
 * No Scaffold - manages its own insets.
 */
@Composable
private fun HomeDetailLayout(
    detailDestination: HomeDestination.DetailDestination,
    onBackFromDetail1: () -> Unit,
    onBackFromDetail2: () -> Unit,
    // ... other callbacks
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        when (detailDestination) {
            is HomeDestination.DetailDestination.ConversationDetail ->
                ConversationDetailRoute(
                    modifier = Modifier.fillMaxSize(),
                    conversationId = detailDestination.conversationId,
                    contactName = detailDestination.contactName,
                    onBack = onBackFromDetail1,
                )
            
            HomeDestination.DetailDestination.Contacts ->
                ContactsRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBackFromDetail2,
                    // ... callbacks
                )
            
            // Add your other detail destinations here
        }
    }
}
```

---

### Phase 6: Add Stability Helpers

#### Step 6.1: Stable Action Wrappers

Add these helper functions at the bottom of your screen file:

```kotlin
/**
 * Wraps a callback to prevent recreation on recomposition.
 * The lambda always calls the latest version of the action.
 */
@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun <T1, T2> rememberStableAction(action: (T1, T2) -> Unit): (T1, T2) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value1, value2 -> actionState.value(value1, value2) } }
}

@Composable
private fun <T> rememberStableProvider(provider: () -> T): () -> T {
    val providerState = rememberUpdatedState(provider)
    return remember { { providerState.value() } }
}
```

**Usage:**
```kotlin
HomeTabsLayout(
    onSelectTab = rememberStableAction(onSelectTab),
    onOpenConversation = rememberStableAction(onOpenConversation),
    phoneContactsProvider = rememberStableProvider(phoneContactsProvider),
)
```

---

## Recomposition Optimization Checklist

After implementing the architecture, apply these optimizations:

### ✅ Rule A: Read State in Narrowest Scope

**Pattern:** Parent reads state only to pass to child
**Fix:** Use `rememberUpdatedState` in LaunchedEffects

```kotlin
val currentDestination by rememberUpdatedState(destination)

LaunchedEffect(Unit) {
    flow.collect {
        // Use currentDestination instead of destination
        if (currentDestination is ...) { }
    }
}
```

### ✅ Rule B: Use remember() for Computed Values

**Pattern:** Values computed on every recomposition
**Fix:** Wrap in `remember()` with appropriate keys

```kotlin
// ❌ BAD
val showBar = condition1 && condition2

// ✅ GOOD
val showBar = remember(condition1, condition2) {
    condition1 && condition2
}
```

### ✅ Rule C: Use derivedStateOf for Fast-Changing State

**Pattern:** Scroll position, animation values
**Fix:**

```kotlin
val showTopBar by remember {
    derivedStateOf { lazyListState.firstVisibleItemIndex > 20 }
}
```

### ✅ Rule D: Stabilize Modifiers

**Pattern:** Modifier chains rebuilt every recomposition
**Fix:**

```kotlin
val contentModifier = remember(padding, visible) {
    Modifier
        .padding(padding)
        .fillMaxSize()
        .then(if (visible) Modifier else Modifier.alpha(0f))
}
```

---

## Testing Checklist

After implementing, verify:

- [ ] **Tab Switching**
  - Smooth horizontal slide between tabs
  - Bottom bar remains visible and stable
  - No content jumps or flickers

- [ ] **Tab → Detail Navigation**
  - Content slides smoothly from tab to detail
  - Bottom bar disappears cleanly
  - **NO vertical content jump before animation**
  - Detail screen has correct insets

- [ ] **Detail → Tab Navigation (Back)**
  - Smooth slide back to tab view
  - Bottom bar reappears cleanly
  - Tab content resumes at same scroll position

- [ ] **Settings Submenus**
  - Bottom bar hides when entering submenu
  - Bottom bar reappears when back to main settings
  - Chrome visibility state works correctly

- [ ] **Notifications/Overlays**
  - Overlays appear on both tab and detail contexts
  - Dismiss actions work correctly
  - No z-index issues

- [ ] **Back Gesture**
  - Physical back button works
  - Swipe back gesture works (if applicable)
  - Correct destination is targeted

- [ ] **Accessibility**
  - TalkBack navigation works
  - Content descriptions are correct
  - Focus management during transitions

- [ ] **Performance**
  - No jank during animations
  - Layout Inspector shows low recomposition counts
  - No memory leaks on repeated navigation

---

## Common Pitfalls & Solutions

### Pitfall 1: Detail Screen Content Still Jumps

**Cause:** Detail screen has its own top bar with padding changes

**Solution:** Make sure detail screens don't depend on parent padding:

```kotlin
// In HomeDetailLayout
Box(
    modifier = modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.navigationBars) // Direct inset handling
) {
    DetailRoute(
        modifier = Modifier.fillMaxSize() // Full size, not depending on parent padding
    )
}
```

### Pitfall 2: Tab State Lost When Navigating to Detail

**Cause:** AnimatedContent recreates tab content

**Solution:** Use proper state hoisting and remember keys:

```kotlin
// In your ViewModel or state holder
val tabStates = remember {
    mutableStateMapOf<HomeTab, Any>() // Store tab-specific state
}
```

### Pitfall 3: Bottom Bar Shows Briefly During Transition

**Cause:** Animation timing mismatch

**Solution:** Use consistent animation durations:

```kotlin
// Root layout transition
slideIn(animationSpec = tween(300))

// Scaffold visibility transition (if animated)
AnimatedVisibility(
    visible = showBottomBar,
    enter = fadeIn(animationSpec = tween(300)),
    exit = fadeOut(animationSpec = tween(300))
)
```

### Pitfall 4: Back Stack Not Working

**Cause:** Navigation state not properly managed

**Solution:** Ensure your ViewModel/state holder handles back navigation:

```kotlin
fun onBackPressed() {
    when (destination) {
        is HomeDestination.DetailDestination -> {
            // Clear detail state
            activeDetailId = null
        }
        is HomeDestination.TabDestination -> {
            // Handle system back (exit app or go to previous screen)
        }
    }
}
```

---

## Advanced: Multi-Module Setup

If your detail screens are in separate modules:

### 1. Define Navigation Contract

```kotlin
// :feature:navigation module
interface DetailScreenFactory {
    @Composable
    fun CreateScreen(
        destination: HomeDestination.DetailDestination,
        onBack: () -> Unit,
        modifier: Modifier = Modifier
    )
}
```

### 2. Implement in Detail Module

```kotlin
// :feature:conversation module
class ConversationDetailFactory : DetailScreenFactory {
    @Composable
    override fun CreateScreen(
        destination: HomeDestination.DetailDestination,
        onBack: () -> Unit,
        modifier: Modifier
    ) {
        when (destination) {
            is HomeDestination.DetailDestination.ConversationDetail ->
                ConversationDetailScreen(
                    conversationId = destination.conversationId,
                    onBack = onBack,
                    modifier = modifier
                )
            else -> { /* Not handled by this factory */ }
        }
    }
}
```

### 3. Compose Factories in Home Module

```kotlin
@Composable
private fun HomeDetailLayout(
    detailDestination: HomeDestination.DetailDestination,
    factories: List<DetailScreenFactory>,
    // ...
) {
    Box(modifier = modifier) {
        factories.forEach { factory ->
            factory.CreateScreen(
                destination = detailDestination,
                onBack = onBack,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

---

## Compiler Configuration (Optional)

Enable Compose metrics for validation:

```kotlin
// app/build.gradle.kts or build.gradle
kotlin {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
        metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
        
        // Optional: Enable strong skipping (Kotlin 2.0+)
        enableStrongSkippingMode = true
    }
}
```

After building, check `build/compose_compiler/reports/` for:
- Composable stability analysis
- Skippable vs restartable composables
- Parameter stability reports

---

## Summary: Why This Works

1. **Sibling Layout Pattern:** Details are not children of the tab Scaffold, so they have independent padding
2. **Type-Safe Destinations:** Sealed class hierarchy enforces categorization at compile-time
3. **Intrinsic Properties:** Animation order lives with the destination, not in external functions
4. **Stable References:** `remember()` and `rememberUpdatedState()` prevent unnecessary recompositions
5. **Clear Responsibilities:** Each composable has one job (tabs, details, or orchestration)

---

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────────┐
│ QUICK REFERENCE: Bottom Bar Jump Fix                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 1. Destination Hierarchy (MODELS)                          │
│    sealed class HomeDestination {                           │
│      sealed class TabDestination(animationOrder: Int)       │
│      sealed class DetailDestination                         │
│    }                                                        │
│                                                             │
│ 2. Layout Mode (SCREEN)                                    │
│    private sealed interface HomeLayout {                    │
│      data class Tabs(destination: TabDestination)           │
│      data class Detail(destination: DetailDestination)      │
│    }                                                        │
│                                                             │
│ 3. Root Structure (MAIN COMPOSABLE)                        │
│    Box {                                                    │
│      AnimatedContent(layout) {                              │
│        when (it) {                                          │
│          Tabs -> HomeTabsLayout(has Scaffold + BottomBar)  │
│          Detail -> HomeDetailLayout(full screen)            │
│        }                                                    │
│      }                                                      │
│    }                                                        │
│                                                             │
│ 4. Tab Layout (PRIVATE COMPOSABLE)                         │
│    Scaffold(bottomBar = { if (show) NavigationBar })       │
│      AnimatedContent(tabDestination) { ... }                │
│                                                             │
│ 5. Detail Layout (PRIVATE COMPOSABLE)                      │
│    Box(windowInsetsPadding) {                              │
│      when (destination) { ... }                             │
│    }                                                        │
│                                                             │
│ KEY: Tabs and Details are SIBLINGS, not parent-child       │
└─────────────────────────────────────────────────────────────┘
```

---

## Complete File Structure

```
your-app/
├── shared/domain/
│   └── models/
│       └── AppNavigationModels.kt      [Phase 1: Destination hierarchy]
│       └── AppUiState.kt                [Phase 1: State mapping]
├── app/
│   └── ui/
│       └── home/
│           └── HomeScreen.kt            [Phases 2-6: Main implementation]
│               ├── HomeLayout (sealed interface)
│               ├── HomeScreen (main composable)
│               ├── HomeTabsLayout (private)
│               ├── HomeDetailLayout (private)
│               └── rememberStable* helpers
└── ARCHITECTURE_PATTERN_GUIDE.md        [This file]
```

---

## Migration Checklist

When applying to a new repository:

- [ ] Phase 1: Create destination hierarchy with sealed classes
- [ ] Phase 1: Update state mapping to use new hierarchy
- [ ] Phase 2: Add HomeLayout sealed interface
- [ ] Phase 3: Refactor main screen to use layout mode
- [ ] Phase 3: Add back gesture handling
- [ ] Phase 4: Extract tab layout composable with Scaffold
- [ ] Phase 5: Extract detail layout composable without Scaffold
- [ ] Phase 6: Add stability helper functions
- [ ] Test all navigation flows
- [ ] Run Layout Inspector for recomposition analysis
- [ ] Optional: Enable Compose compiler metrics
- [ ] Document any project-specific variations

---

**Created:** 2026-01-27  
**Pattern Version:** 1.0  
**Target:** Jetpack Compose applications with tab + detail navigation  
**Compatibility:** Compose 1.5+, Kotlin 1.9+

---

## Support & Troubleshooting

If you encounter issues:

1. **Read the error message** - Type errors will point to destination mismatches
2. **Check Phase 1** - Ensure all destinations are categorized correctly
3. **Verify Modifiers** - Details should use `.windowInsetsPadding()`, tabs should use Scaffold padding
4. **Test in isolation** - Comment out animations to verify layout structure first
5. **Use Layout Inspector** - Visual debugging is essential for Compose issues

Good luck! This pattern has been battle-tested and follows Android best practices. 🚀
