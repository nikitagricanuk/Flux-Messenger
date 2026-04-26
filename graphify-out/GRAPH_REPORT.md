# Graph Report - .  (2026-04-25)

## Corpus Check
- 147 files · ~61,474 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 706 nodes · 1066 edges · 70 communities detected
- Extraction: 68% EXTRACTED · 32% INFERRED · 0% AMBIGUOUS · INFERRED: 338 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Passkey Authentication (Backend)|Passkey Authentication (Backend)]]
- [[_COMMUNITY_JWT & OAuth Security (Backend)|JWT & OAuth Security (Backend)]]
- [[_COMMUNITY_Chat & User Data Layer|Chat & User Data Layer]]
- [[_COMMUNITY_Android Auth Utilities|Android Auth Utilities]]
- [[_COMMUNITY_Android UI Fragments|Android UI Fragments]]
- [[_COMMUNITY_Android API Client Layer|Android API Client Layer]]
- [[_COMMUNITY_Android Activities & OAuth|Android Activities & OAuth]]
- [[_COMMUNITY_Android Login UI|Android Login UI]]
- [[_COMMUNITY_OAuth Identity & Google|OAuth Identity & Google]]
- [[_COMMUNITY_OAuth API & GitHub|OAuth API & GitHub]]
- [[_COMMUNITY_Android Auth Interceptor|Android Auth Interceptor]]
- [[_COMMUNITY_Chat Adapter & UI|Chat Adapter & UI]]
- [[_COMMUNITY_Architecture Documentation|Architecture Documentation]]
- [[_COMMUNITY_User REST Controller|User REST Controller]]
- [[_COMMUNITY_Exception Handling|Exception Handling]]
- [[_COMMUNITY_Contact & Message Adapters|Contact & Message Adapters]]
- [[_COMMUNITY_Security Configuration|Security Configuration]]
- [[_COMMUNITY_Chat REST Controller|Chat REST Controller]]
- [[_COMMUNITY_Passkey Registration Types|Passkey Registration Types]]
- [[_COMMUNITY_Login Form State|Login Form State]]
- [[_COMMUNITY_Auth REST Controller|Auth REST Controller]]
- [[_COMMUNITY_Sign-Up Request DTO|Sign-Up Request DTO]]
- [[_COMMUNITY_Passkey Verifier|Passkey Verifier]]
- [[_COMMUNITY_Logged-In User View|Logged-In User View]]
- [[_COMMUNITY_Application Tests|Application Tests]]
- [[_COMMUNITY_OpenAPI Config|OpenAPI Config]]
- [[_COMMUNITY_Database Initializer|Database Initializer]]
- [[_COMMUNITY_Spring App Entry Point|Spring App Entry Point]]
- [[_COMMUNITY_OAuth Identity Entity|OAuth Identity Entity]]
- [[_COMMUNITY_Passkey Entity|Passkey Entity]]
- [[_COMMUNITY_Passkey Auth Finish Request|Passkey Auth Finish Request]]
- [[_COMMUNITY_Passkey Reg Finish Request|Passkey Reg Finish Request]]
- [[_COMMUNITY_GitHub API Client|GitHub API Client]]
- [[_COMMUNITY_Chat Request Validator|Chat Request Validator]]
- [[_COMMUNITY_Android Instrumented Tests|Android Instrumented Tests]]
- [[_COMMUNITY_Android Unit Tests|Android Unit Tests]]
- [[_COMMUNITY_Update User Request|Update User Request]]
- [[_COMMUNITY_Contact Entity|Contact Entity]]
- [[_COMMUNITY_Login Request DTO|Login Request DTO]]
- [[_COMMUNITY_OAuth Code Exchange|OAuth Code Exchange]]
- [[_COMMUNITY_Auth Service Docs|Auth Service Docs]]
- [[_COMMUNITY_Sign-In Request DTO|Sign-In Request DTO]]
- [[_COMMUNITY_OAuth Complete Request|OAuth Complete Request]]
- [[_COMMUNITY_Google OAuth Request|Google OAuth Request]]
- [[_COMMUNITY_OAuth Login Response|OAuth Login Response]]
- [[_COMMUNITY_Passkey Auth Start Response|Passkey Auth Start Response]]
- [[_COMMUNITY_GitHub OAuth Request|GitHub OAuth Request]]
- [[_COMMUNITY_JWT Auth Response DTO|JWT Auth Response DTO]]
- [[_COMMUNITY_Contact Response DTO|Contact Response DTO]]
- [[_COMMUNITY_User Response DTO|User Response DTO]]
- [[_COMMUNITY_Chat Response DTO|Chat Response DTO]]
- [[_COMMUNITY_Android Auth Interceptor (Legacy)|Android Auth Interceptor (Legacy)]]
- [[_COMMUNITY_User Identity Design|User Identity Design]]
- [[_COMMUNITY_App Launcher Icon (mdpi)|App Launcher Icon (mdpi)]]
- [[_COMMUNITY_App Icon Round (mdpi)|App Icon Round (mdpi)]]
- [[_COMMUNITY_App Launcher Icon (hdpi)|App Launcher Icon (hdpi)]]
- [[_COMMUNITY_App Icon Round (hdpi)|App Icon Round (hdpi)]]
- [[_COMMUNITY_Google OAuth Icon|Google OAuth Icon]]
- [[_COMMUNITY_GitHub Logo Icon|GitHub Logo Icon]]
- [[_COMMUNITY_GitHub Icon (ic_github)|GitHub Icon (ic_github)]]
- [[_COMMUNITY_Profile Screen Background|Profile Screen Background]]
- [[_COMMUNITY_App Wallpaper Background|App Wallpaper Background]]
- [[_COMMUNITY_Google Branding Image|Google Branding Image]]
- [[_COMMUNITY_App Background Image|App Background Image]]
- [[_COMMUNITY_App Launcher Icon (xxxhdpi)|App Launcher Icon (xxxhdpi)]]
- [[_COMMUNITY_App Icon Round (xxxhdpi)|App Icon Round (xxxhdpi)]]
- [[_COMMUNITY_App Launcher Icon (xxhdpi)|App Launcher Icon (xxhdpi)]]
- [[_COMMUNITY_App Icon Round (xxhdpi)|App Icon Round (xxhdpi)]]
- [[_COMMUNITY_App Launcher Icon (xhdpi)|App Launcher Icon (xhdpi)]]
- [[_COMMUNITY_App Icon Round (xhdpi)|App Icon Round (xhdpi)]]

## God Nodes (most connected - your core abstractions)
1. `UserService` - 19 edges
2. `UserController` - 16 edges
3. `SettingsProfileFragment` - 13 edges
4. `OAuthManager` - 13 edges
5. `JwtService` - 12 edges
6. `PasskeyService` - 12 edges
7. `LoginViewModel` - 11 edges
8. `LoginRepository` - 11 edges
9. `JwtServiceTest` - 9 edges
10. `UserServiceOAuthTest` - 9 edges

## Surprising Connections (you probably didn't know these)
- `Backend Module (Spring Boot 4, Java 21, PostgreSQL)` --references--> `WebSocket (STOMP)`  [INFERRED]
  CLAUDE.md → docs/HELP.md
- `Backend Module (Spring Boot 4, Java 21, PostgreSQL)` --references--> `Spring Boot OAuth2`  [INFERRED]
  CLAUDE.md → docs/HELP.md
- `Multi-module Gradle Project` --references--> `Gradle (build system)`  [EXTRACTED]
  CLAUDE.md → docs/HELP.md
- `Backend Module (Spring Boot 4, Java 21, PostgreSQL)` --references--> `Spring Security`  [EXTRACTED]
  CLAUDE.md → docs/HELP.md
- `Backend Module (Spring Boot 4, Java 21, PostgreSQL)` --references--> `Spring Data JPA`  [EXTRACTED]
  CLAUDE.md → docs/HELP.md

## Hyperedges (group relationships)
- **JWT Security Flow** — claude_md_jwt_filter, claude_md_jwt_service, claude_md_security_config, claude_md_auth_controller, claude_md_authentication_service [EXTRACTED 0.95]

## Communities

### Community 0 - "Passkey Authentication (Backend)"
Cohesion: 0.06
Nodes (11): ChallengeStore, ChallengeStoreTest, PasskeyAssertionOptionsResponse, PasskeyAssertionRequest, Response, PasskeyController, PasskeyService, PasskeyServiceTest (+3 more)

### Community 1 - "JWT & OAuth Security (Backend)"
Cohesion: 0.07
Nodes (9): AuthenticationService, JwtAuthenticationFilter, JwtService, JwtServiceTest, OAuthCompletionService, OAuthCompletionServiceTest, OncePerRequestFilter, User (+1 more)

### Community 2 - "Chat & User Data Layer"
Cohesion: 0.07
Nodes (5): Chat, ChatRepository, ChatService, UserRepository, UserService

### Community 3 - "Android Auth Utilities"
Cohesion: 0.05
Nodes (13): ChatNotFoundException, JWTUtil, LoginDataSource, OAuthVerificationException, Callback, PasskeyAuthManager, PasskeyVerificationException, Error (+5 more)

### Community 4 - "Android UI Fragments"
Cohesion: 0.07
Nodes (7): ChatsFragment, Fragment, SettingsFragment, SettingsNotificationsFragment, SettingsProfileFragment, SignUpCompletionInternalFragment, WelcomeAuthFragment

### Community 5 - "Android API Client Layer"
Cohesion: 0.07
Nodes (9): AndroidViewModel, ApiClient, ApiService, AuthRepositoryFactory, BottomSheetDialogFragment, LoginViewModelFactory, NewMessageBottomSheet, RetrofitClient (+1 more)

### Community 6 - "Android Activities & OAuth"
Cohesion: 0.09
Nodes (6): AppCompatActivity, GitHubOAuthService, LoginActivity, MainActivity, OAuthCallbackPayload, OAuthManager

### Community 7 - "Android Login UI"
Cohesion: 0.07
Nodes (7): LoginFragment, LoginViewModel, PhoneTextWatcher, SignUpAuthFragment, SignUpCompletion3rdPartyFragment, TextWatcher, ViewModel

### Community 8 - "OAuth Identity & Google"
Cohesion: 0.1
Nodes (6): GoogleOAuthService, LoggedInUser, OAuthLoginService, OAuthLoginServiceTest, UserOAuthIdentityRepository, UserServiceOAuthTest

### Community 9 - "OAuth API & GitHub"
Cohesion: 0.09
Nodes (6): AuthApi, GitHubApiClient, GitHubApiClientImpl, GitHubOAuthServiceTest, GoogleOAuthServiceTest, OAuthController

### Community 10 - "Android Auth Interceptor"
Cohesion: 0.11
Nodes (4): AuthInterceptor, Interceptor, LoginRepository, LoginResult

### Community 11 - "Chat Adapter & UI"
Cohesion: 0.12
Nodes (5): ChatAdapter, ChatViewHolder, LinearLayout, OnTabSelectedListener, SegmentTabsView

### Community 12 - "Architecture Documentation"
Cohesion: 0.11
Nodes (22): Android Module (minSdk 26, Java 11), shared/api-contract (DTOs and domain models, Java 17), ApiService (Retrofit HTTP interface), Backend Module (Spring Boot 4, Java 21, PostgreSQL), Rationale: cleartext HTTP enabled for dev against local backend, shared/client-sdk (wraps api-contract + core), shared/core (shared utilities, Java 17), Docker Compose Infrastructure (Postgres + backend) (+14 more)

### Community 13 - "User REST Controller"
Cohesion: 0.16
Nodes (1): UserController

### Community 14 - "Exception Handling"
Cohesion: 0.2
Nodes (1): GlobalExceptionHandler

### Community 15 - "Contact & Message Adapters"
Cohesion: 0.22
Nodes (2): ContactViewHolder, NewMessageAdapter

### Community 16 - "Security Configuration"
Cohesion: 0.38
Nodes (1): SecurityConfig

### Community 17 - "Chat REST Controller"
Cohesion: 0.29
Nodes (1): ChatController

### Community 18 - "Passkey Registration Types"
Cohesion: 0.29
Nodes (6): AuthenticatorSelection, Descriptor, PasskeyRegistrationStartResponse, PubKeyCredParam, RelyingParty, UserInfo

### Community 19 - "Login Form State"
Cohesion: 0.33
Nodes (1): LoginFormState

### Community 20 - "Auth REST Controller"
Cohesion: 0.4
Nodes (1): AuthController

### Community 21 - "Sign-Up Request DTO"
Cohesion: 0.5
Nodes (1): SignUpRequest

### Community 22 - "Passkey Verifier"
Cohesion: 0.5
Nodes (1): PasskeyVerifier

### Community 23 - "Logged-In User View"
Cohesion: 0.5
Nodes (1): LoggedInUserView

### Community 24 - "Application Tests"
Cohesion: 0.67
Nodes (1): FluxApplicationTests

### Community 25 - "OpenAPI Config"
Cohesion: 0.67
Nodes (1): OpenApiConfig

### Community 26 - "Database Initializer"
Cohesion: 0.67
Nodes (1): LoadDatabase

### Community 27 - "Spring App Entry Point"
Cohesion: 0.67
Nodes (1): FluxApplication

### Community 28 - "OAuth Identity Entity"
Cohesion: 0.67
Nodes (1): UserOAuthIdentity

### Community 29 - "Passkey Entity"
Cohesion: 0.67
Nodes (1): UserPasskey

### Community 30 - "Passkey Auth Finish Request"
Cohesion: 0.67
Nodes (2): PasskeyAuthenticationFinishRequest, Response

### Community 31 - "Passkey Reg Finish Request"
Cohesion: 0.67
Nodes (2): PasskeyRegistrationFinishRequest, Response

### Community 32 - "GitHub API Client"
Cohesion: 0.67
Nodes (1): GitHubApiClient

### Community 33 - "Chat Request Validator"
Cohesion: 0.67
Nodes (1): ChatRequestValidator

### Community 34 - "Android Instrumented Tests"
Cohesion: 0.67
Nodes (1): ExampleInstrumentedTest

### Community 35 - "Android Unit Tests"
Cohesion: 0.67
Nodes (1): ExampleUnitTest

### Community 36 - "Update User Request"
Cohesion: 0.67
Nodes (1): UpdateUserRequest

### Community 37 - "Contact Entity"
Cohesion: 0.67
Nodes (1): Contact

### Community 38 - "Login Request DTO"
Cohesion: 0.67
Nodes (1): LoginRequest

### Community 39 - "OAuth Code Exchange"
Cohesion: 0.67
Nodes (1): OAuthCodeExchangeRequest

### Community 40 - "Auth Service Docs"
Cohesion: 0.67
Nodes (3): AuthController (POST /api/auth/sign-up and /sign-in), AuthenticationService, JwtAuthenticationResponse (access token 15min + refresh token 7 days)

### Community 41 - "Sign-In Request DTO"
Cohesion: 1.0
Nodes (1): SignInRequest

### Community 42 - "OAuth Complete Request"
Cohesion: 1.0
Nodes (1): OAuthCompleteRequest

### Community 43 - "Google OAuth Request"
Cohesion: 1.0
Nodes (1): GoogleOAuthRequest

### Community 44 - "OAuth Login Response"
Cohesion: 1.0
Nodes (1): OAuthLoginResponse

### Community 45 - "Passkey Auth Start Response"
Cohesion: 1.0
Nodes (1): PasskeyAuthenticationStartResponse

### Community 46 - "GitHub OAuth Request"
Cohesion: 1.0
Nodes (1): GitHubOAuthRequest

### Community 47 - "JWT Auth Response DTO"
Cohesion: 1.0
Nodes (1): JwtAuthenticationResponse

### Community 48 - "Contact Response DTO"
Cohesion: 1.0
Nodes (1): ContactResponse

### Community 49 - "User Response DTO"
Cohesion: 1.0
Nodes (1): UserResponse

### Community 50 - "Chat Response DTO"
Cohesion: 1.0
Nodes (1): ChatResponse

### Community 51 - "Android Auth Interceptor (Legacy)"
Cohesion: 1.0
Nodes (1): AuthInceptor

### Community 52 - "User Identity Design"
Cohesion: 1.0
Nodes (2): Rationale: phone field used as Spring Security principal (getUsername returns phone), User (implements UserDetails, getUsername returns phone)

### Community 73 - "App Launcher Icon (mdpi)"
Cohesion: 1.0
Nodes (1): App Launcher Icon (mdpi) — default Android green robot

### Community 74 - "App Icon Round (mdpi)"
Cohesion: 1.0
Nodes (1): App Icon Round (mdpi) — default Android green robot on circular green background

### Community 75 - "App Launcher Icon (hdpi)"
Cohesion: 1.0
Nodes (1): App Launcher Icon (hdpi) — default Android robot, green square background

### Community 76 - "App Icon Round (hdpi)"
Cohesion: 1.0
Nodes (1): App Icon Round (hdpi) — Android robot silhouette white on green, circular

### Community 77 - "Google OAuth Icon"
Cohesion: 1.0
Nodes (1): Google OAuth Icon — multicolor G logo for OAuth sign-in button

### Community 78 - "GitHub Logo Icon"
Cohesion: 1.0
Nodes (1): GitHub Logo Icon — black Octocat on circular background

### Community 79 - "GitHub Icon (ic_github)"
Cohesion: 1.0
Nodes (1): GitHub Icon (ic_github) — white Octocat silhouette on black circle

### Community 80 - "Profile Screen Background"
Cohesion: 1.0
Nodes (1): Profile Screen Background — radial gradient coral-orange to lavender-blue

### Community 81 - "App Wallpaper Background"
Cohesion: 1.0
Nodes (1): App Wallpaper Background — soft pastel gradient blue/purple to pink

### Community 82 - "Google Branding Image"
Cohesion: 1.0
Nodes (1): Google Branding Image — Google G logo in brand colors

### Community 83 - "App Background Image"
Cohesion: 1.0
Nodes (1): App Background Image — soft pastel gradient light blue to lavender-purple

### Community 84 - "App Launcher Icon (xxxhdpi)"
Cohesion: 1.0
Nodes (1): App Launcher Icon (xxxhdpi) — green grid background, white Android robot, drop shadow

### Community 85 - "App Icon Round (xxxhdpi)"
Cohesion: 1.0
Nodes (1): App Icon Round (xxxhdpi) — green grid background, white Android robot, circular

### Community 86 - "App Launcher Icon (xxhdpi)"
Cohesion: 1.0
Nodes (1): App Launcher Icon (xxhdpi) — rounded-square, green grid, white Android robot

### Community 87 - "App Icon Round (xxhdpi)"
Cohesion: 1.0
Nodes (1): App Icon Round (xxhdpi) — white Android bugdroid on mint-green grid, circular

### Community 88 - "App Launcher Icon (xhdpi)"
Cohesion: 1.0
Nodes (1): App Launcher Icon (xhdpi) — light green grid, white Android robot, drop shadow

### Community 89 - "App Icon Round (xhdpi)"
Cohesion: 1.0
Nodes (1): App Icon Round (xhdpi) — white Android robot on green grid, circular clip

## Knowledge Gaps
- **54 isolated node(s):** `PasskeyAuthenticationFinishRequest`, `Response`, `SignInRequest`, `OAuthCompleteRequest`, `GoogleOAuthRequest` (+49 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `User REST Controller`** (17 nodes): `UserController.java`, `UserController`, `.addContact()`, `.addContactToMe()`, `.createUser()`, `.deleteMe()`, `.deleteUserById()`, `.getAllUsers()`, `.getContacts()`, `.getMe()`, `.getMyContacts()`, `.getUserById()`, `.removeContact()`, `.removeContactToMe()`, `.updateMe()`, `.updateUser()`, `.UserController()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Exception Handling`** (10 nodes): `GlobalExceptionHandler.java`, `GlobalExceptionHandler`, `.handleAlreadyExists()`, `.handleAuthVerification()`, `.handleBadRequest()`, `.handleConflict()`, `.handleJwtException()`, `.handleNotFound()`, `.handleRegistrationTokenExpired()`, `.handleValidation()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Contact & Message Adapters`** (9 nodes): `NewMessageAdapter.java`, `ContactViewHolder`, `.ContactViewHolder()`, `NewMessageAdapter`, `.getItemCount()`, `.NewMessageAdapter()`, `.onBindViewHolder()`, `.onCreateViewHolder()`, `.setContacts()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Security Configuration`** (7 nodes): `SecurityConfig.java`, `SecurityConfig`, `.authenticationManager()`, `.authenticationProvider()`, `.jwtFilterRegistration()`, `.passwordEncoder()`, `.securityFilterChain()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Chat REST Controller`** (7 nodes): `ChatController.java`, `ChatController`, `.ChatController()`, `.createChat()`, `.deleteChatById()`, `.getAllChats()`, `.getChatById()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Login Form State`** (6 nodes): `LoginFormState.java`, `LoginFormState`, `.getPasswordError()`, `.getPhoneError()`, `.isDataValid()`, `.LoginFormState()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Auth REST Controller`** (5 nodes): `AuthController.java`, `AuthController`, `.refresh()`, `.signIn()`, `.signUp()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Sign-Up Request DTO`** (4 nodes): `SignUpRequest.java`, `SignUpRequest.java`, `SignUpRequest`, `.SignUpRequest()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Passkey Verifier`** (4 nodes): `PasskeyVerifier.java`, `PasskeyVerifier`, `.verifyAuthentication()`, `.verifyRegistration()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Logged-In User View`** (4 nodes): `LoggedInUserView.java`, `LoggedInUserView`, `.getDisplayName()`, `.LoggedInUserView()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Application Tests`** (3 nodes): `FluxApplicationTests.java`, `FluxApplicationTests`, `.contextLoads()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OpenAPI Config`** (3 nodes): `OpenApiConfig.java`, `OpenApiConfig`, `.openAPI()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Database Initializer`** (3 nodes): `LoadDatabase.java`, `LoadDatabase`, `.initDatabase()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Spring App Entry Point`** (3 nodes): `FluxApplication.java`, `FluxApplication`, `.main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth Identity Entity`** (3 nodes): `UserOAuthIdentity.java`, `UserOAuthIdentity`, `.onCreate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Passkey Entity`** (3 nodes): `UserPasskey.java`, `UserPasskey`, `.onCreate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Passkey Auth Finish Request`** (3 nodes): `PasskeyAuthenticationFinishRequest.java`, `PasskeyAuthenticationFinishRequest`, `Response`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Passkey Reg Finish Request`** (3 nodes): `PasskeyRegistrationFinishRequest.java`, `PasskeyRegistrationFinishRequest`, `Response`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GitHub API Client`** (3 nodes): `GitHubApiClient.java`, `GitHubApiClient`, `.exchange()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Chat Request Validator`** (3 nodes): `ChatRequestValidator.java`, `ChatRequestValidator`, `.isValid()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Instrumented Tests`** (3 nodes): `ExampleInstrumentedTest.java`, `ExampleInstrumentedTest`, `.useAppContext()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Unit Tests`** (3 nodes): `ExampleUnitTest.java`, `ExampleUnitTest`, `.addition_isCorrect()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Update User Request`** (3 nodes): `UpdateUserRequest.java`, `UpdateUserRequest`, `.UpdateUserRequest()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Contact Entity`** (3 nodes): `Contact.java`, `Contact`, `.Contact()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Login Request DTO`** (3 nodes): `LoginRequest.java`, `LoginRequest`, `.LoginRequest()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth Code Exchange`** (3 nodes): `OAuthCodeExchangeRequest.java`, `OAuthCodeExchangeRequest`, `.OAuthCodeExchangeRequest()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Sign-In Request DTO`** (2 nodes): `SignInRequest.java`, `SignInRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth Complete Request`** (2 nodes): `OAuthCompleteRequest.java`, `OAuthCompleteRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Google OAuth Request`** (2 nodes): `GoogleOAuthRequest.java`, `GoogleOAuthRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth Login Response`** (2 nodes): `OAuthLoginResponse.java`, `OAuthLoginResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Passkey Auth Start Response`** (2 nodes): `PasskeyAuthenticationStartResponse.java`, `PasskeyAuthenticationStartResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GitHub OAuth Request`** (2 nodes): `GitHubOAuthRequest.java`, `GitHubOAuthRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `JWT Auth Response DTO`** (2 nodes): `JwtAuthenticationResponse.java`, `JwtAuthenticationResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Contact Response DTO`** (2 nodes): `ContactResponse.java`, `ContactResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Response DTO`** (2 nodes): `UserResponse.java`, `UserResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Chat Response DTO`** (2 nodes): `ChatResponse.java`, `ChatResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Auth Interceptor (Legacy)`** (2 nodes): `AuthInceptor.java`, `AuthInceptor`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Identity Design`** (2 nodes): `Rationale: phone field used as Spring Security principal (getUsername returns phone)`, `User (implements UserDetails, getUsername returns phone)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Launcher Icon (mdpi)`** (1 nodes): `App Launcher Icon (mdpi) — default Android green robot`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Icon Round (mdpi)`** (1 nodes): `App Icon Round (mdpi) — default Android green robot on circular green background`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Launcher Icon (hdpi)`** (1 nodes): `App Launcher Icon (hdpi) — default Android robot, green square background`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Icon Round (hdpi)`** (1 nodes): `App Icon Round (hdpi) — Android robot silhouette white on green, circular`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Google OAuth Icon`** (1 nodes): `Google OAuth Icon — multicolor G logo for OAuth sign-in button`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GitHub Logo Icon`** (1 nodes): `GitHub Logo Icon — black Octocat on circular background`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GitHub Icon (ic_github)`** (1 nodes): `GitHub Icon (ic_github) — white Octocat silhouette on black circle`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Profile Screen Background`** (1 nodes): `Profile Screen Background — radial gradient coral-orange to lavender-blue`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Wallpaper Background`** (1 nodes): `App Wallpaper Background — soft pastel gradient blue/purple to pink`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Google Branding Image`** (1 nodes): `Google Branding Image — Google G logo in brand colors`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Background Image`** (1 nodes): `App Background Image — soft pastel gradient light blue to lavender-purple`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Launcher Icon (xxxhdpi)`** (1 nodes): `App Launcher Icon (xxxhdpi) — green grid background, white Android robot, drop shadow`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Icon Round (xxxhdpi)`** (1 nodes): `App Icon Round (xxxhdpi) — green grid background, white Android robot, circular`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Launcher Icon (xxhdpi)`** (1 nodes): `App Launcher Icon (xxhdpi) — rounded-square, green grid, white Android robot`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Icon Round (xxhdpi)`** (1 nodes): `App Icon Round (xxhdpi) — white Android bugdroid on mint-green grid, circular`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Launcher Icon (xhdpi)`** (1 nodes): `App Launcher Icon (xhdpi) — light green grid, white Android robot, drop shadow`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Icon Round (xhdpi)`** (1 nodes): `App Icon Round (xhdpi) — white Android robot on green grid, circular clip`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserService` connect `Chat & User Data Layer` to `OAuth Identity & Google`, `JWT & OAuth Security (Backend)`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `SettingsProfileFragment` connect `Android UI Fragments` to `JWT & OAuth Security (Backend)`?**
  _High betweenness centrality (0.020) - this node is a cross-community bridge._
- **Why does `ChatsFragment` connect `Android UI Fragments` to `Chat Adapter & UI`, `Android API Client Layer`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **What connects `PasskeyAuthenticationFinishRequest`, `Response`, `SignInRequest` to the rest of the system?**
  _54 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Passkey Authentication (Backend)` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `JWT & OAuth Security (Backend)` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Chat & User Data Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._