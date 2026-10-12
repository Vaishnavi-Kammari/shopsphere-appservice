# ShopSphere

A simple e-commerce app for learning: **React + Vite** frontend, **Spring Boot 3 (Java 21)** backend, **PostgreSQL** database.

```
shopsphere/
├── backend/    Spring Boot API (Controller -> Service -> Repository, Spring Data JPA, Spring Security)
└── frontend/   React single-page app (Vite, Axios, React Router)
```

## Prerequisites

| Tool | Version | Check with |
|------|---------|-----------|
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Node.js | 18+ (20/22 recommended) | `node -v` |
| PostgreSQL | 14+ | `psql --version` |

---

## 1. PostgreSQL setup (local)

Install PostgreSQL (e.g. `sudo apt install postgresql` on Ubuntu, `brew install postgresql@16 && brew services start postgresql@16` on macOS, or the EDB installer on Windows), then create the database and user.

**Linux:**
```bash
sudo -u postgres psql
```
**macOS (Homebrew):** `psql postgres`  
**Windows:** open *SQL Shell (psql)* and log in as `postgres`.

Then run:
```sql
CREATE USER shopsphere WITH PASSWORD 'change_me_db_password';
CREATE DATABASE shopsphere OWNER shopsphere;
\q
```
Use your own password; it must match `DB_PASSWORD` below. Verify the connection:
```bash
psql -h localhost -U shopsphere -d shopsphere -c "select 1"
```
You do **not** create tables yourself: the backend creates them on first start (`spring.jpa.hibernate.ddl-auto=update`).

---

## 2. Configure and start the backend

```bash
cd backend
cp .env.example .env          # Windows PowerShell: Copy-Item .env.example .env
```
Edit `backend/.env` and set at least `DB_PASSWORD` (same as above) and `TOKEN_SECRET` (32+ random characters, e.g. `openssl rand -base64 48`).

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/shopsphere` | JDBC URL |
| `DB_USERNAME` | no | `shopsphere` | DB user |
| `DB_PASSWORD` | **yes** | - | DB password |
| `TOKEN_SECRET` | **yes** | - | Signs login tokens (>= 32 chars) |
| `TOKEN_EXPIRY_HOURS` | no | `8` | Login lifetime |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5173` | Comma-separated frontend origins |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | no | `admin@shopsphere.local` / `Admin@123` | Admin account created at startup |
| `SEED_DEMO_DATA` | no | `true` | Create sample data if DB is empty |
| `PORT` | no | `8080` | HTTP port |

Real environment variables work too and take precedence over `.env`, which is how you will configure Azure later.

Start it:
```bash
cd backend
mvn spring-boot:run
```
Check it: <http://localhost:8080/api/health> should return `{"status":"UP",...}`.

Optional runnable jar: `mvn clean package` then `java -jar target/shopsphere-backend-1.0.0.jar` (run it from the `backend` folder so `.env` is found).

## 3. Configure and start the frontend

In a second terminal:
```bash
cd frontend
cp .env.example .env          # Windows PowerShell: Copy-Item .env.example .env
npm install
npm run dev
```
Open <http://localhost:5173>.

`frontend/.env` holds `VITE_API_BASE_URL=http://localhost:8080/api`. Production build: `npm run build` (output in `frontend/dist`). Note that `VITE_` variables are baked in at build time.

---

## 4. Development credentials (local use only!)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@shopsphere.local` | `Admin@123` |
| Customer | `customer@shopsphere.local` | `Customer@123` |

The admin comes from `ADMIN_EMAIL` / `ADMIN_PASSWORD`; the customer is created only when `SEED_DEMO_DATA=true`. Change the admin password and set `SEED_DEMO_DATA=false` anywhere other than your laptop. You can also register new customers on the Login page. The login page shows "Fill admin / Fill customer" buttons in dev mode only (`npm run dev`).

Sample data: 5 categories and 14 products (one deliberately out of stock). Product photos load from picsum.photos; offline, a coloured tile is shown instead.

---

## 5. Manual testing guide

**Customer flow**
1. Open `/` - see the hero, category chips and featured products.
2. Go to **Products**, search "coffee", filter by category *Electronics*, clear filters.
3. Open a product: details, stock and quantity picker appear. Click **Add to cart** while logged out -> redirected to Login.
4. Log in as the customer -> returned to the product; add 2 units. Navbar cart badge shows 2.
5. **Cart**: use +/- (limited by stock), **Remove** an item, add another product.
6. Enter a shipping address and **Place order** -> redirected to **Orders** with a success banner; cart is empty; total matches.
7. Open *Trail Running Shoes* (stock 0): no add button, "Out of stock".
8. Stock check: add all available units of *Adjustable Dumbbell Set* (8), place the order, then try adding one more -> "Only 0 ... in stock".

**Admin flow**
1. Log out, log in as admin -> lands on **Admin**. (As a customer, `/admin` redirects home.)
2. **Products** tab: add a product, edit its price/stock, delete it (confirm dialog).
3. **Orders** tab: see the customer's order, change status to PROCESSING -> SHIPPED -> DELIVERED. Cancelling an order returns its stock; a cancelled order cannot be reopened.
4. Log in as the customer again: Orders shows the new status.

**API checks (curl)**
```bash
curl -s localhost:8080/api/health
curl -s "localhost:8080/api/products?search=coffee"
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/cart                      # 401 (not logged in)
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"customer@shopsphere.local","password":"Customer@123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" localhost:8080/api/cart          # 200
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" localhost:8080/api/admin/orders  # 403 (customer)
curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"customer@shopsphere.local","password":"wrong"}'                                              # 401 + message
```

## API summary

| Method & path | Access | Notes |
|---------------|--------|-------|
| `GET /api/health` | public | |
| `POST /api/auth/register`, `POST /api/auth/login` | public | returns `{token, user}` |
| `GET /api/auth/me` | login | |
| `GET /api/products?search=&categoryId=`, `GET /api/products/{id}`, `GET /api/categories` | public | |
| `GET /api/cart`, `POST /api/cart/items`, `PUT/DELETE /api/cart/items/{productId}` | login | stock validated |
| `POST /api/orders`, `GET /api/orders`, `GET /api/orders/{id}` | login | total and stock computed server-side |
| `POST/PUT/DELETE /api/admin/products[/{id}]`, `GET /api/admin/orders`, `PUT /api/admin/orders/{id}/status` | ADMIN | |

Status codes: 200/201/204 success, 400 validation, 401 not logged in / bad credentials, 403 wrong role, 404 not found, 409 conflict (email taken, insufficient stock).

## How authentication works

`POST /api/auth/login` checks the BCrypt-hashed password and returns a signed token (HMAC-SHA256 with `TOKEN_SECRET`, with an expiry). React stores it in `localStorage` and sends `Authorization: Bearer <token>` on each request; a Spring Security filter validates it and loads the user's role. There are no cookies or server sessions, so the API and the frontend can live on different Azure hosts. (For a real production system you would likely use an identity provider such as Microsoft Entra ID.)

## Troubleshooting

* `Could not resolve placeholder 'DB_PASSWORD'` - `backend/.env` is missing, or you started the app from the wrong folder. Run from `backend/`.
* `password authentication failed` - `DB_PASSWORD` differs from the one used in `CREATE USER`.
* `TOKEN_SECRET must be at least 32 characters long` - lengthen it.
* Browser shows "Cannot reach the server" / CORS error - backend not running, wrong `VITE_API_BASE_URL`, or the frontend origin is not listed in `CORS_ALLOWED_ORIGINS`.
* Port 8080 or 5173 busy - set `PORT` for the backend; the frontend (`vite`) picks the next port automatically, then add that origin to `CORS_ALLOWED_ORIGINS`.

## Notes for the later Azure deployment

Everything is configured by environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `TOKEN_SECRET`, `CORS_ALLOWED_ORIGINS`, `ADMIN_PASSWORD`, `SEED_DEMO_DATA`, `PORT`), and the frontend by `VITE_API_BASE_URL` at build time. Azure Database for PostgreSQL requires SSL, so you will add `?sslmode=require` to `DB_URL`. `GET /api/health` is ready to use as a health probe.
