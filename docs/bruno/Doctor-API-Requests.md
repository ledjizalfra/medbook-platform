# MedBook Platform - Doctor DMN API - Tutte le Request JSON

## 1. CREATE DOCTOR - POST /api/v1/doctors

Registra un nuovo medico sulla piattaforma.

### Request Body:
```json
{
  "firstName": "Luca",
  "lastName": "Bianchi",
  "dateOfBirth": "1975-06-20",
  "gender": "MASCHILE",
  "email": "luca.bianchi@medbook.it",
  "phone": "+393331234567",
  "licenseNumber": "RM-12345"
}
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Response (201 Created):
```json
{
  "httpStatus": 201,
  "success": true,
  "timestamp": "2026-03-29T10:30:00Z",
  "traceId": "6f4d2a1b3c8e9f0a",
  "data": {
    "doctorId": "DOC-1"
  }
}
```

---

## 2. GET ALL DOCTORS - GET /api/v1/doctors

Restituisce la lista paginata dei medici.

### Query Parameters:
- `page` (int, default: 0) - Numero pagina (0-based)
- `size` (int, default: 20) - Elementi per pagina
- `sort` (string, default: "") - Ordinamento (es: lastName,asc)
- `status` (string, optional) - Filtro stato: ACTIVE, INACTIVE, SUSPENDED
- `lastName` (string, optional) - Filtro cognome (ricerca parziale case-insensitive)
- `specialization` (string, optional) - Filtro specializzazione medica

### URL Examples:
```
GET /api/v1/doctors?page=0&size=20&sort=lastName,asc
GET /api/v1/doctors?page=0&size=10&status=ACTIVE
GET /api/v1/doctors?lastName=Bianchi&specialization=CARDIOLOGY
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:35:00Z",
  "traceId": "6f4d2a1b3c8e9f0b",
  "data": [
    {
      "doctorId": "DOC-1",
      "firstName": "Luca",
      "lastName": "Bianchi",
      "email": "luca.bianchi@medbook.it",
      "licenseNumber": "RM-12345",
      "status": "ATTIVO"
    },
    {
      "doctorId": "DOC-2",
      "firstName": "Marco",
      "lastName": "Rossi",
      "email": "marco.rossi@medbook.it",
      "licenseNumber": "RM-67890",
      "status": "ATTIVO"
    }
  ],
  "page": {
    "totalElements": 2,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "first": true,
    "last": true,
    "empty": false
  }
}
```

---

## 3. GET DOCTOR BY ID - GET /api/v1/doctors/{doctorId}

Restituisce il dettaglio completo di un medico tramite business key.

### Path Parameters:
- `doctorId` (string, required) - Business key del medico (es: DOC-1)

### URL Example:
```
GET /api/v1/doctors/DOC-1
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:40:00Z",
  "traceId": "6f4d2a1b3c8e9f0c",
  "data": {
    "doctorId": "DOC-1",
    "firstName": "Luca",
    "lastName": "Bianchi",
    "dateOfBirth": "1975-06-20",
    "gender": "MASCHILE",
    "email": "luca.bianchi@medbook.it",
    "phone": "+393331234567",
    "licenseNumber": "RM-12345",
    "status": "ATTIVO",
    "createdAt": "2026-03-20T09:00:00Z",
    "updatedAt": "2026-03-29T10:40:00Z"
  }
}
```

### Response (404 Not Found):
```json
{
  "httpStatus": 404,
  "success": false,
  "timestamp": "2026-03-29T10:40:00Z",
  "traceId": "6f4d2a1b3c8e9f0d",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Doctor with businessKey DOC-999 not found",
  "path": "/api/v1/doctors/DOC-999"
}
```

---

## 4. UPDATE DOCTOR - PATCH /api/v1/doctors/{doctorId}

Aggiorna parzialmente i dati di un medico. Solo i campi presenti nel body vengono aggiornati.

### Path Parameters:
- `doctorId` (string, required) - Business key del medico (es: DOC-1)

### Request Body (tutti i campi opzionali):
```json
{
  "firstName": "Luca",
  "lastName": "Bianchi",
  "dateOfBirth": "1975-06-20",
  "email": "luca.bianchi.updated@medbook.it",
  "phone": "+393339999999",
  "status": "ATTIVO"
}
```

**Campi non modificabili:**
- `doctorId` - Business key
- `licenseNumber` - Numero di iscrizione all'Ordine
- `gender` - Genere

### URL Example:
```
PATCH /api/v1/doctors/DOC-1
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:45:00Z",
  "traceId": "6f4d2a1b3c8e9f0e"
}
```

### Response (404 Not Found):
```json
{
  "httpStatus": 404,
  "success": false,
  "timestamp": "2026-03-29T10:45:00Z",
  "traceId": "6f4d2a1b3c8e9f0f",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Doctor with businessKey DOC-999 not found",
  "path": "/api/v1/doctors/DOC-999"
}
```

---

## 5. DELETE DOCTOR - DELETE /api/v1/doctors/{doctorId}

Esegue il soft delete di un medico (il record rimane sul DB con deleted = true).

### Path Parameters:
- `doctorId` (string, required) - Business key del medico (es: DOC-1)

### URL Example:
```
DELETE /api/v1/doctors/DOC-1
```

### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:50:00Z",
  "traceId": "6f4d2a1b3c8e9f10"
}
```

### Response (404 Not Found):
```json
{
  "httpStatus": 404,
  "success": false,
  "timestamp": "2026-03-29T10:50:00Z",
  "traceId": "6f4d2a1b3c8e9f11",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Doctor with businessKey DOC-999 not found",
  "path": "/api/v1/doctors/DOC-999"
}
```

---

## ENUM VALUES

### GenderApiEnum
```
MALE
FEMALE
```

### DoctorStatusApiEnum
```
ACTIVE      - medico operativo
INACTIVE    - medico non piu attivo nella struttura
SUSPENDED   - medico temporaneamente sospeso
```

### MedicalSpecializationApiEnum
```
CARDIOLOGY
DERMATOLOGY
ENDOCRINOLOGY
GASTROENTEROLOGY
GENERAL_MEDICINE
GYNECOLOGY
INTERNAL_MEDICINE
NEUROLOGY
ONCOLOGY
OPHTHALMOLOGY
ORTHOPEDICS
OTOLARYNGOLOGY
PEDIATRICS
PSYCHIATRY
PULMONOLOGY
RADIOLOGY
RHEUMATOLOGY
UROLOGY
```

---

## ESEMPI AGGIUNTIVI

### Create Doctor - Esempio completo
```json
POST /api/v1/doctors
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "firstName": "Marco",
  "lastName": "Rossi",
  "dateOfBirth": "1980-05-15",
  "gender": "MASCHILE",
  "email": "marco.rossi@medbook.it",
  "phone": "+393334567890",
  "licenseNumber": "RM-54321"
}
```

### Create Doctor - Minimal (solo required fields)
```json
POST /api/v1/doctors
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "firstName": "Giulia",
  "lastName": "Verdi",
  "gender": "FEMMINILE",
  "email": "giulia.verdi@medbook.it",
  "phone": "+393335678901",
  "licenseNumber": "MI-99999"
}
```

### Update Doctor - Minimal update (solo email)
```json
PATCH /api/v1/doctors/DOC-1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "email": "luca.bianchi.new@medbook.it"
}
```

### Update Doctor - Cambia lo stato a sospeso
```json
PATCH /api/v1/doctors/DOC-1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "status": "SOSPESO"
}
```

### Get All Doctors - Paginazione
```
GET /api/v1/doctors?page=0&size=10&sort=lastName,asc
```

### Get All Doctors - Filtro per stato e specializzazione
```
GET /api/v1/doctors?page=0&size=20&status=ACTIVE&specialization=CARDIOLOGY
```

### Get All Doctors - Filtro per cognome
```
GET /api/v1/doctors?page=0&size=20&lastName=Rossi
```

---

## ERROR RESPONSES

### 400 Bad Request - Validation Error
```json
{
  "httpStatus": 400,
  "success": false,
  "timestamp": "2026-03-29T10:55:00Z",
  "traceId": "6f4d2a1b3c8e9f12",
  "errorCode": "VALIDATION_ERROR",
  "message": [
    "firstName: cannot be blank",
    "email: invalid email format",
    "licenseNumber: cannot be blank"
  ],
  "path": "/api/v1/doctors"
}
```

### 409 Conflict - Duplicate Email/LicenseNumber
```json
{
  "httpStatus": 409,
  "success": false,
  "timestamp": "2026-03-29T10:55:00Z",
  "traceId": "6f4d2a1b3c8e9f13",
  "errorCode": "UNIQUE_CONSTRAINT_VIOLATION",
  "message": "Email luca.bianchi@medbook.it already exists in the system",
  "path": "/api/v1/doctors"
}
```

### 401 Unauthorized - Missing or Invalid Token
```json
{
  "httpStatus": 401,
  "success": false,
  "timestamp": "2026-03-29T10:55:00Z",
  "traceId": "6f4d2a1b3c8e9f14",
  "errorCode": "UNAUTHORIZED",
  "message": "Missing or invalid JWT token",
  "path": "/api/v1/doctors"
}
```

### 403 Forbidden - Insufficient Permissions
```json
{
  "httpStatus": 403,
  "success": false,
  "timestamp": "2026-03-29T10:55:00Z",
  "traceId": "6f4d2a1b3c8e9f15",
  "errorCode": "ACCESS_DENIED",
  "message": "User role DOCTOR cannot perform this operation",
  "path": "/api/v1/doctors"
}
```

---

## VARIABILI DI AMBIENTE (da configurare in Bruno)

```
base_url_doctor=http://localhost:8091
access_token=your_jwt_token_here
```

---

## NOTE IMPORTANTI

1. **Business Key**: Tutti gli ID seguono il formato `DOC-{seq}` (es. DOC-1, DOC-42)
2. **Soft Delete**: Il DELETE non elimina fisicamente il record dal DB, ma lo marca come cancellato
3. **PATCH vs PUT**: Si usa PATCH per aggiornamenti parziali
4. **Authentication**: Tutti gli endpoint richiedono un JWT valido
5. **MedBookContext**: Viene iniettato automaticamente dal sistema tramite interceptor
6. **Trace ID**: Utile per il debugging - correla i log end-to-end
7. **Paginazione**: Di default page=0, size=20
8. **Unicità**: Email e licenseNumber devono essere univoci nel sistema

