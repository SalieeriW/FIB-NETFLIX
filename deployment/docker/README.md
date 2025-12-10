# Step 1: Docker Containerization

## What is Docker?

Docker packages your application and its dependencies into a **container** - a lightweight, portable unit that runs the same way everywhere.

**Why Docker?**
- Consistent environment (dev, staging, prod)
- Easy deployment
- Isolation between services
- Resource efficiency

## Our Services

We need to containerize:
1. **Python ML Service** (FastAPI)
2. **Java REST Service** (GlassFish)
3. **Web Client** (Static files + JSP)
4. **Database** (Derby or MySQL)

Let's start with the Python service - it's the most complex.

