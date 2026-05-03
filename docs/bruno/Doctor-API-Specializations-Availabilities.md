# MedBook Platform - Doctor DMN API - Specializations & Availabilities

## SPECIALIZATIONS ENDPOINTS

### 1. CREATE SPECIALIZATION - POST /api/v1/doctors/{doctorId}/specializations

Aggiunge una nuova specializzazione medica al medico.

#### Request Body:
```json
{
  "specialization": "CARDIOLOGIA",
  "isPrimary": true
}
```

#### URL Example:
```
POST /api/v1/doctors/DOC-1/specializations
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (201 Created):
```json
{
  "httpStatus": 201,
  "success": true,
  "timestamp": "2026-03-29T10:55:00Z",
  "traceId": "6f4d2a1b3c8e9f0a",
  "data": {
    "specializationId": "SPC-1"
  }
}
```

#### Notes:
- Un medico può avere più specializzazioni
- Solo un record per medico può avere `isPrimary = true`
- Se non specificato, `isPrimary` viene impostato a `false`

---

### 2. GET ALL SPECIALIZATIONS - GET /api/v1/doctors/{doctorId}/specializations

Restituisce tutte le specializzazioni di un medico.

#### URL Example:
```
GET /api/v1/doctors/DOC-1/specializations
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:56:00Z",
  "traceId": "6f4d2a1b3c8e9f0b",
  "data": {
    "items": [
      {
        "specializationId": "SPC-1",
        "doctorId": "DOC-1",
        "specialization": "CARDIOLOGIA",
        "isPrimary": true,
        "status": "ATTIVO",
        "createdAt": "2026-03-20T09:00:00Z",
        "updatedAt": "2026-03-29T10:56:00Z"
      },
      {
        "specializationId": "SPC-2",
        "doctorId": "DOC-1",
        "specialization": "INTERNAL_MEDICINE",
        "isPrimary": false,
        "status": "ATTIVO",
        "createdAt": "2026-03-21T09:00:00Z",
        "updatedAt": "2026-03-29T10:56:00Z"
      }
    ]
  }
}
```

---

### 3. UPDATE SPECIALIZATION - PATCH /api/v1/doctors/{doctorId}/specializations/{specializationId}

Aggiorna i dati di una specializzazione.

#### Path Parameters:
- `doctorId` - Business key del medico (es: DOC-1)
- `specializationId` - Business key della specializzazione (es: SPC-1)

#### Request Body (tutti i campi opzionali):
```json
{
  "isPrimary": true,
  "status": "ATTIVO"
}
```

#### URL Example:
```
PATCH /api/v1/doctors/DOC-1/specializations/SPC-1
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:57:00Z",
  "traceId": "6f4d2a1b3c8e9f0c"
}
```

#### Notes:
- Se `isPrimary` viene impostato a `true`, tutti gli altri record `isPrimary = true` per lo stesso medico verranno automaticamente impostati a `false`

---

### 4. DELETE SPECIALIZATION - DELETE /api/v1/doctors/{doctorId}/specializations/{specializationId}

Elimina una specializzazione del medico.

#### URL Example:
```
DELETE /api/v1/doctors/DOC-1/specializations/SPC-1
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T10:58:00Z",
  "traceId": "6f4d2a1b3c8e9f0d"
}
```

#### Error (409 Conflict):
```json
{
  "httpStatus": 409,
  "success": false,
  "timestamp": "2026-03-29T10:58:00Z",
  "traceId": "6f4d2a1b3c8e9f0e",
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "message": "Cannot delete the only specialization of the doctor",
  "path": "/api/v1/doctors/DOC-1/specializations/SPC-1"
}
```

#### Notes:
- Non è possibile eliminare l'unica specializzazione di un medico
- Il medico deve avere almeno una specializzazione

---

## AVAILABILITIES ENDPOINTS

### 5. CREATE AVAILABILITY - POST /api/v1/doctors/{doctorId}/availabilities

Crea un template di disponibilità settimanale ricorrente per il medico presso una sede.

#### Path Parameters:
- `doctorId` - Business key del medico (es: DOC-1)

#### Request Body:
```json
{
  "clinicId": "CLN-1",
  "dayOfWeek": "LUNEDI",
  "startTime": "09:00",
  "endTime": "13:00",
  "slotDurationMinutes": 30
}
```

#### URL Example:
```
POST /api/v1/doctors/DOC-1/availabilities
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (201 Created):
```json
{
  "httpStatus": 201,
  "success": true,
  "timestamp": "2026-03-29T11:00:00Z",
  "traceId": "6f4d2a1b3c8e9f0f",
  "data": {
    "availabilityId": "DAV-1"
  }
}
```

#### Notes:
- Genera slot concreti tramite job schedulato in appointment-dmn
- Un medico non può avere due template per la stessa sede nello stesso giorno
- Esempio: fascia 09:00-13:00 (4 ore = 240 minuti) con slot da 30 minuti = 8 slot = 8 pazienti ricevibili
- La disponibilità è gestita esclusivamente dall'amministrazione

---

### 6. GET ALL AVAILABILITIES - GET /api/v1/doctors/{doctorId}/availabilities

Restituisce tutti i template di disponibilità di un medico.

#### Path Parameters:
- `doctorId` - Business key del medico (es: DOC-1)

#### Query Parameters (tutti opzionali):
- `clinicId` - Filtra per sede (es: CLN-1)
- `dayOfWeek` - Filtra per giorno della settimana (es: MONDAY)
- `status` - Filtra per stato (ACTIVE, INACTIVE)

#### URL Examples:
```
GET /api/v1/doctors/DOC-1/availabilities
GET /api/v1/doctors/DOC-1/availabilities?clinicId=CLN-1
GET /api/v1/doctors/DOC-1/availabilities?dayOfWeek=MONDAY&status=ACTIVE
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T11:01:00Z",
  "traceId": "6f4d2a1b3c8e9f10",
  "data": {
    "items": [
      {
        "availabilityId": "DAV-1",
        "doctorId": "DOC-1",
        "clinicId": "CLN-1",
        "dayOfWeek": "LUNEDI",
        "startTime": "09:00",
        "endTime": "13:00",
        "slotDurationMinutes": 30,
        "status": "ATTIVO",
        "createdAt": "2026-03-20T09:00:00Z",
        "updatedAt": "2026-03-29T11:01:00Z"
      },
      {
        "availabilityId": "DAV-2",
        "doctorId": "DOC-1",
        "clinicId": "CLN-1",
        "dayOfWeek": "MARTEDI",
        "startTime": "14:00",
        "endTime": "18:00",
        "slotDurationMinutes": 30,
        "status": "ATTIVO",
        "createdAt": "2026-03-20T09:00:00Z",
        "updatedAt": "2026-03-29T11:01:00Z"
      }
    ]
  }
}
```

---

### 7. UPDATE AVAILABILITY - PATCH /api/v1/doctors/{doctorId}/availabilities/{availabilityId}

Aggiorna un template di disponibilità.

#### Path Parameters:
- `doctorId` - Business key del medico (es: DOC-1)
- `availabilityId` - Business key del template (es: DAV-1)

#### Request Body (tutti i campi opzionali):
```json
{
  "startTime": "08:00",
  "endTime": "14:00",
  "slotDurationMinutes": 45,
  "status": "ATTIVO"
}
```

#### URL Example:
```
PATCH /api/v1/doctors/DOC-1/availabilities/DAV-1
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T11:02:00Z",
  "traceId": "6f4d2a1b3c8e9f11"
}
```

#### Notes:
- La modifica di `clinicId` o `dayOfWeek` richiede verifica di unicità
- `endTime` deve essere successiva a `startTime`

---

### 8. DELETE AVAILABILITY - DELETE /api/v1/doctors/{doctorId}/availabilities/{availabilityId}

Esegue il soft delete di un template di disponibilità.

#### Path Parameters:
- `doctorId` - Business key del medico (es: DOC-1)
- `availabilityId` - Business key del template (es: DAV-1)

#### URL Example:
```
DELETE /api/v1/doctors/DOC-1/availabilities/DAV-1
```

#### Headers:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### Response (200 OK):
```json
{
  "httpStatus": 200,
  "success": true,
  "timestamp": "2026-03-29T11:03:00Z",
  "traceId": "6f4d2a1b3c8e9f12"
}
```

#### Notes:
- Soft delete: il record rimane nel DB con `deleted = true`
- Gli slot futuri già generati verranno cancellati tramite evento Kafka `medbook.availability.changed`
- Solo ROLE_ADMIN e ROLE_RECEPTIONIST (propria sede) possono eseguire

---

## ENUM VALUES FOR AVAILABILITIES

### DayOfWeekApiEnum
```
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

### AvailabilityStatusApiEnum
```
ACTIVE      - template attivo - gli slot vengono generati
INACTIVE    - template sospeso - nessun nuovo slot viene generato
```

---

## ESEMPIO COMPLETO - Flusso di creazione medico con specializzazioni e disponibilità

### Passo 1: Crea medico
```json
POST /api/v1/doctors

{
  "firstName": "Luca",
  "lastName": "Bianchi",
  "dateOfBirth": "1975-06-20",
  "gender": "MASCHILE",
  "email": "luca.bianchi@medbook.it",
  "phone": "+393331234567",
  "licenseNumber": "RM-12345"
}

Response: {"doctorId": "DOC-1"}
```

### Passo 2: Aggiungi specializzazione principale
```json
POST /api/v1/doctors/DOC-1/specializations

{
  "specialization": "CARDIOLOGIA",
  "isPrimary": true
}

Response: {"specializationId": "SPC-1"}
```

### Passo 3: Aggiungi specializzazione secondaria
```json
POST /api/v1/doctors/DOC-1/specializations

{
  "specialization": "INTERNAL_MEDICINE",
  "isPrimary": false
}

Response: {"specializationId": "SPC-2"}
```

### Passo 4: Crea template di disponibilità lunedì
```json
POST /api/v1/doctors/DOC-1/availabilities

{
  "clinicId": "CLN-1",
  "dayOfWeek": "LUNEDI",
  "startTime": "09:00",
  "endTime": "13:00",
  "slotDurationMinutes": 30
}

Response: {"availabilityId": "DAV-1"}
```

### Passo 5: Crea template di disponibilità martedì
```json
POST /api/v1/doctors/DOC-1/availabilities

{
  "clinicId": "CLN-1",
  "dayOfWeek": "MARTEDI",
  "startTime": "14:00",
  "endTime": "18:00",
  "slotDurationMinutes": 30
}

Response: {"availabilityId": "DAV-2"}
```

### Passo 6: Recupera tutto il profilo del medico
```
GET /api/v1/doctors/DOC-1
```

### Passo 7: Recupera specializzazioni del medico
```
GET /api/v1/doctors/DOC-1/specializations
```

### Passo 8: Recupera disponibilità del medico
```
GET /api/v1/doctors/DOC-1/availabilities
```

---

## AUTORIZZAZIONI RICHIESTE

| Endpoint | ROLE_ADMIN | ROLE_RECEPTIONIST | ROLE_DOCTOR | BFF |
|----------|----------|----------|----------|-----|
| POST /doctors | ✓ | ✗ | ✗ | ✗ |
| GET /doctors | ✓ | ✓ | ✗ | ✗ |
| GET /doctors/{id} | ✓ | ✓ | Solo proprio | ✗ |
| PATCH /doctors/{id} | ✓ | ✗ | ✗ | ✗ |
| DELETE /doctors/{id} | ✓ | ✗ | ✗ | ✗ |
| POST /specializations | ✓ | ✗ | ✗ | ✗ |
| GET /specializations | ✓ | ✓ | Solo proprie | ✓ |
| PATCH /specializations/{id} | ✓ | ✗ | ✗ | ✗ |
| DELETE /specializations/{id} | ✓ | ✗ | ✗ | ✗ |
| POST /availabilities | ✓ | ✓ (propria sede) | ✗ | ✗ |
| GET /availabilities | ✓ | ✓ | Solo proprie | ✓ |
| PATCH /availabilities/{id} | ✓ | ✓ (propria sede) | ✗ | ✗ |
| DELETE /availabilities/{id} | ✓ | ✓ (propria sede) | ✗ | ✗ |

