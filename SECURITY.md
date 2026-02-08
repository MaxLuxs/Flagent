# Security Policy

## Supported Versions

We release security updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability, please report it responsibly:

1. **Do not** open a public GitHub issue for security vulnerabilities.
2. **Email** [max.developer.luxs@gmail.com](mailto:max.developer.luxs@gmail.com) with:
   - Description of the vulnerability
   - Steps to reproduce
   - Affected versions
   - Suggested fix (if any)
3. We will acknowledge receipt within 48 hours.
4. We will provide a timeline for resolution and keep you updated.
5. After the fix is released, we will credit you in the security advisory (unless you prefer to remain anonymous).

## Response Timeline

- **Critical**: Response within 24–48 hours, fix within 7 days
- **High**: Response within 1 week, fix within 14 days
- **Medium/Low**: Response within 2 weeks, fix in next release cycle

## Security Practices

- **Dependencies**: We use Dependabot and regular security scans (CodeQL, Trivy).
- **Secrets**: Never commit API keys, tokens, or credentials. Use environment variables.
- **HTTPS**: All network communication uses HTTPS.
- **Auth**: API keys and tokens are validated on every request.

Thanks for helping keep Flagent and our users safe.
