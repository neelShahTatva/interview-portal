# API-AUTH001: Authenticate User (Login)

## 1. API Metadata
| Attribute | Detail |
| :--- | :--- |
| **API ID** | API-AUTH001 |
| **Name** | User Login |
| **Description** | Authenticates a user using email and password, returning JWT access and refresh tokens. |
| **Method** | `POST` |
| **Endpoint** | `/auth/login` |
| **Status** | Active |
| **Owner** | Authentication / Security Team |

---

## 2. Request

### 2.1 Headers
| Header Name | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `Content-Type` | String | Yes | `application/json` |

### 2.2 Request Body *(Note: Replaces Query Params for POST)*
| Field Name | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `email` | String | Yes | `null` | The registered email address of the user. |
| `password` | String | Yes | `null` | The raw password for the account. |

### 2.3 Enum Values of Request Params
* **N/A** - No enums are passed in the login request payload.

---

## 3. Response

### 3.1 Success
**HTTP Status:** `200 OK`

#### JSON Content
```json
{
  "statusCode": 200,
  "success": true,
  "errorMessages": null,
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NT...",
    "refreshToken": "dGhpcy1pcy1hLXJlZnJlc2gtdG9rZW4...",
    "user": {
      "id": 101,
      "username": "tatva_user",
      "password": null,
      "email": "user@tatvasoft.com",
      "isActive": true,
      "role": {
        "id": 1,
        "name": "ROLE_ADMIN"
      },
      "createdAt": "2026-01-15T10:00:00",
      "updatedAt": "2026-06-03T14:30:00",
      "createdBy": 1,
      "updatedBy": 1,
      "resetToken": null,
      "resetTokenExpiry": null
    }
  }
}
```
#### 3.1.1 Content Fields
### 3.1.1 Content Fields

| Field Path | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| `statusCode` | `Integer` | No | Standardized HTTP status code representation. |
| `success` | `Boolean` | No | Indicates if the operation was successful. |
| `errorMessages` | `Array` | Yes | List of error messages (`null` on success). |
| `result.accessToken` | `String` | No | JWT token used for authenticating subsequent API requests. |
| `result.refreshToken` | `String` | No | Token used to generate a new access token without re-logging in. |
| `result.user.id` | `Integer` | No | Unique identifier for the authenticated user. |
| `result.user.username` | `String` | No | The unique login alphanumeric name of the user. |
| `result.user.password` | `String` | Yes | The password string (Explicitly set to `null` during serialization for security). |
| `result.user.email` | `String` | No | Email address of the authenticated user. |
| `result.user.isActive` | `Boolean` | No | Boolean flag indicating if the account is currently active. |
| `result.user.role` | `Object` | No | Embedded object representing the associated role group. |
| `result.user.role.id` | `Integer` | No | Unique identifier for the role entity. |
| `result.user.role.name` | `String` | No | Code designation name of the role (e.g., `ROLE_ADMIN`). |
| `result.user.createdAt` | `String` | No | Timestamp recording when the user record was initialized (ISO 8601). |
| `result.user.updatedAt` | `String` | No | Timestamp recording the last profile modification event (ISO 8601). |
| `result.user.createdBy` | `Integer` | Yes | The user ID responsible for executing the registration event. |
| `result.user.updatedBy` | `Integer` | Yes | The user ID responsible for managing the last persistence modification. |
| `result.user.resetToken` | `String` | Yes | Temporary UUID sequence utilized exclusively for pending forgot-password challenges. |
| `result.user.resetTokenExpiry`| `String` | Yes | Expiration ceiling boundary timestamp targeting active reset token cycles. |


#### 3.1.2 Pagination Fields
| Field Path | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| *N/A* | - | - | Pagination is not applicable for this endpoint. |

#### 3.1.3 Persistence View
The data for this API is retrieved using the following core SQL query to fetch the user record before password verification:

```sql
SELECT
    u.id,
    u.username,
    u.password,
    u.email,
    u.is_active,
    u.role_id,
    u.created_at,
    u.updated_at,
    u.created_by,
    u.updated_by,
    u.reset_token,
    u.reset_token_expiry
FROM users u
WHERE u.email = :email;
```

### 3.2 Error Response

#### 3.2.1 Expected HTTP Status Codes
| HTTP Status Code | Expected Error Message | Description |
| :--- | :--- | :--- |
| `400 Bad Request` | "Refresh token is required." <br> "Invalid or expired refresh token" | The request is malformed, missing the required parameter, or the provided token is invalid/expired. |
| `401 Unauthorized` | "Unauthorized access" | The user is not authenticated or a required authorization header is missing. |
| `500 Internal Server Error` | "Internal Server Error" | An unexpected system exception occurred on the server. |

#### 3.3.2 Standard Error JSON Format
All error responses return the standardized API wrapper structure with the `result` field set to `null`. The `statusCode` will dynamically match the HTTP status.

```json
{
  "statusCode": 400,
  "success": false,
  "errorMessages": [
    "Invalid or expired refresh token"
  ],
  "result": null
}
```

### 3.4 Conditional Logic & End-to-End Validation Flow

#### 3.4.1 Request-to-Response Execution Pipeline
This pipeline tracks the exact sequential lifecycle of an inbound login request across the application layers:

| Sequence | Layer Component | Action / Operation Description | Success Path | Failure / Exception Path |
| :--- | :--- | :--- | :--- | :--- |
| **1. Validation** | `AuthController` | Intercepts HTTP `POST` request. Validates fields inside `LoginRequest` using `@Valid`/validation constraints. | If fields are populated, pass DTO to `AuthService.login()`. | If `email` or `password` is blank: Throws exception $\rightarrow$ Returns `400 Bad Request`. |
| **2. Fetch Entity** | `AuthService` | Queries `UserRepository` to find the `User` entity matching the exact `email` string. | User entity found $\rightarrow$ Proceed to credentials check. | If User not found: Throws `UnauthorizedException` $\rightarrow$ Returns `401` with "Invalid credentials". |
| **3. Match Pass** | `AuthService` | Passes the raw request password and database `password_hash` to the encoder (e.g., `BCrypt.matches()`). | Password match confirmed $\rightarrow$ Proceed to status check. | If Password mismatch: Throws `UnauthorizedException` $\rightarrow$ Returns `401` with "Invalid credentials". |
| **4. Check Status** | `AuthService` | Evaluates the boolean condition of `user.getIsActive()`. | If `isActive == true` $\rightarrow$ Proceed to session token generation. | If `isActive == false`: Throws `UnauthorizedException` $\rightarrow$ Returns `401` with "User account is inactive". |
| **5. Token Gen** | `AuthService` | Invokes `JwtUtil` to generate two distinct cryptographically signed strings: `accessToken` and `refreshToken`. | Token strings successfully generated. | Any signature/crypto fault $\rightarrow$ Triggers global fallback $\rightarrow$ Returns `500 Internal Error`. |
| **6. Map DTO** | `AuthService` | Instantiates a new `LoginResponse` DTO, binding the tokens and the sanitized `User` entity details inside it. | DTO fully populated $\rightarrow$ Returned back to the controller layer. | — |
| **7. Wrap API** | `AuthController` | Receives the `LoginResponse` DTO and encapsulates it inside the standardized `ApiResponse` constructor. | Enforced fields compiled: `statusCode: 200`, `success: true`, `result: LoginResponse`. | — |
| **8. Dispatch** | `AuthController` | Commits the completed wrapper payload into a `ResponseEntity.ok()`. | Returns HTTP Status `200 OK` back to the API client. | — |

#### 3.4.2 Business Logic & Security Assertions
* **Deterministic Masking:** Steps 2 and 3 must deliberately output identical `401` error arrays (`["Invalid credentials"]`). This guarantees no discrepancy in processing time or message delivery can be exploited by an attacker attempting email enumeration.
* **Primitive Fallback Safe-Guards:** The `isActive` account check evaluated at Step 4 assumes strict null-safey checks (`!Boolean.TRUE.equals(...)`) to prevent accidental unauthorized entry if an unexpected `null` database flag bypasses traditional logical tests.


## 4. Integration Test Matrix

### 4.1 Integration Test Matrix Request
| Scenario ID | Test Case | Body: `email` | Body: `password` |
| :--- | :--- | :--- | :--- |
| `TC_01` | Valid login credentials | `active@tatvasoft.com` | `ValidPass123!` |
| `TC_02` | Invalid password | `active@tatvasoft.com` | `WrongPassword` |
| `TC_03` | Non-existent email | `nobody@tatvasoft.com` | `AnyPassword123!` |
| `TC_04` | Inactive user account | `inactive@tatvasoft.com` | `ValidPass123!` |
| `TC_05` | Missing email in payload | *None* | `ValidPass123!` |

### 4.2 Response Field Expectation
| Scenario ID | Expected Status Code | `success` flag | `result` object | Specific Field Checks |
| :--- | :--- | :--- | :--- | :--- |
| `TC_01` | `200 OK` | `true` | Must not be `null` | `accessToken` and `refreshToken` must be in a valid JWT format. |
| `TC_02` | `401 Unauthorized` | `false` | `null` | `errorMessages` contains "Invalid credentials". |
| `TC_03` | `401 Unauthorized` | `false` | `null` | `errorMessages` contains "Invalid credentials". |
| `TC_04` | `401 Unauthorized` | `false` | `null` | `errorMessages` contains "User account is inactive". |
| `TC_05` | `400 Bad Request` | `false` | `null` | `errorMessages` contains "Email is required.". |

### 4.3 Field Validation Map
| Field | Validation Rule | Expected Error Code | Expected Error Message |
| :--- | :--- | :--- | :--- |
| `email` | Cannot be null or blank. | `ERR_400_BAD_REQ` | "Email is required." |
| `password` | Cannot be null or blank. | `ERR_400_BAD_REQ` | "Password is required." |