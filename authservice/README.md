# Authentication Service with AWS SNS

Secure backend authentication service with phone verification using Amazon SNS and email verification.

## Features

- User registration with email and phone verification
- Phone OTP via Amazon SNS
- Email OTP via SMTP
- JWT-based authentication
- Password reset with dual verification
- Secure password encryption with BCrypt
- CORS enabled for frontend integration

## Prerequisites

1. **Java 17+**
2. **PostgreSQL Database**
3. **AWS Account** with SNS access
4. **Email Account** (Gmail recommended)

## AWS SNS Setup

### 1. Create IAM User for SNS

1. Go to AWS Console → IAM → Users → Create User
2. Attach policy: `AmazonSNSFullAccess`
3. Create access keys (Access Key ID & Secret Access Key)

### 2. Enable SMS in SNS

1. Go to AWS Console → SNS → Text messaging (SMS)
2. Request production access if needed (sandbox allows limited numbers)
3. Set spending limit and default message type

### 3. Phone Number Format

Use E.164 format: `+[country code][phone number]`
- Example: `+12345678900` (US)
- Example: `+919876543210` (India)

## Configuration

### 1. Database Setup

```sql
CREATE DATABASE authdb;
```

### 2. Update application.yaml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/authdb
    username: your_postgres_username
    password: your_postgres_password
  mail:
    username: your_email@gmail.com
    password: your_app_password  # Generate from Google Account settings

aws:
  region: us-east-1  # Your AWS region
  accessKeyId: YOUR_AWS_ACCESS_KEY
  secretAccessKey: YOUR_AWS_SECRET_KEY

jwt:
  secret: your-secret-key-minimum-256-bits-long-string
  expiration: 86400000  # 24 hours
```

### 3. Gmail App Password

1. Enable 2-Factor Authentication on Gmail
2. Go to Google Account → Security → App passwords
3. Generate app password for "Mail"
4. Use this password in application.yaml

## Build & Run

```bash
cd authservice
mvn clean install
mvn spring-boot:run
```

Server runs on: `http://localhost:8080`

## API Endpoints

### Registration Flow

1. **Send OTP**
   ```
   POST /auth/register/send-otp
   Body: {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "phone": "+12345678900",
     "password": "password123"
   }
   ```

2. **Verify OTP**
   ```
   POST /auth/register/verify
   Body: {
     "email": "john@example.com",
     "phone": "+12345678900",
     "emailOtp": "123456",
     "phoneOtp": "654321"
   }
   ```

3. **Resend OTP**
   ```
   POST /auth/register/resend-otp
   Body: {
     "email": "john@example.com",
     "phone": "+12345678900"
   }
   ```

### Login

```
POST /auth/login
Body: {
  "email": "john@example.com",
  "password": "password123"
}
Response: {
  "token": "jwt_token",
  "message": "Login successful"
}
```

### Forgot Password Flow

1. **Send OTP**
   ```
   POST /auth/forgot/send-otp
   Body: {
     "email": "john@example.com",
     "phone": "+12345678900"
   }
   Note: OTP sent only to phone via AWS SNS
   ```

2. **Verify OTP**
   ```
   POST /auth/forgot/verify
   Body: {
     "email": "john@example.com",
     "phone": "+12345678900",
     "phoneOtp": "654321"
   }
   Response: {
     "token": "reset_token"
   }
   Note: Only phone OTP required
   ```

3. **Reset Password**
   ```
   POST /auth/reset-password
   Body: {
     "email": "john@example.com",
     "token": "reset_token",
     "password": "newpassword123"
   }
   ```

## Security Features

- **Password Encryption**: BCrypt with salt
- **JWT Tokens**: Signed with HS256
- **OTP Expiration**: 10 minutes
- **Rate Limiting**: Implement in production
- **CORS**: Configured for localhost:5173 and localhost:3000
- **Input Validation**: Jakarta Validation
- **SQL Injection Protection**: JPA/Hibernate

## Frontend Integration

Update your frontend `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Production Checklist

- [ ] Use environment variables for sensitive data
- [ ] Enable AWS SNS production access
- [ ] Set up proper CORS origins
- [ ] Implement rate limiting
- [ ] Add logging and monitoring
- [ ] Use HTTPS
- [ ] Set strong JWT secret (256+ bits)
- [ ] Configure database connection pooling
- [ ] Add request/response logging
- [ ] Implement account lockout after failed attempts

## Troubleshooting

### SMS Not Sending
- Check AWS SNS sandbox mode (only verified numbers work)
- Verify phone number format (E.164)
- Check AWS credentials and permissions
- Review CloudWatch logs in AWS

### Email Not Sending
- Verify Gmail app password
- Check firewall/antivirus blocking port 587
- Enable "Less secure app access" if needed

### Database Connection Failed
- Verify PostgreSQL is running
- Check database credentials
- Ensure database exists

## Cost Considerations

- **AWS SNS**: ~$0.00645 per SMS (US)
- **Gmail**: Free for low volume
- Consider AWS SNS spending limits

## License

MIT
