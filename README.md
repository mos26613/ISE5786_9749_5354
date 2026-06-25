# 🎨 Java Ray Tracer

> A from-scratch **ray-tracing renderer** written in pure Java — no graphics libraries, just math, light, and clean object-oriented design.

Built for the course **“Mini Project in Introduction to Software Engineering” (151055)**, this renderer grows stage by stage from a handful of geometric primitives into a full physically-inspired rendering engine: shadows, reflections, transparent glass, soft area lights, anti-aliasing, multithreading, and spatial acceleration structures.

<p align="center">
  <img src="images/pisaGoldenHour.png" alt="Leaning Tower of Pisa rendered at golden hour with soft shadows" width="80%">
  <br>
  <em>The Leaning Tower of Pisa at golden hour — soft shadows, multiple light sources, hundreds of geometries.</em>
</p>

---

## 📑 Table of Contents

- [Features](#-features)
- [Gallery](#-gallery)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [How It Works](#-how-it-works)
- [Design Principles](#-design-principles)
- [Roadmap](#-roadmap)

---

## ✨ Features

| Category | What it does |
|---|---|
| **Geometric primitives** | Points, vectors, rays, and full 3-D vector algebra with safe floating-point comparisons |
| **Shapes** | Sphere, Plane, Triangle, Polygon, Tube, Cylinder — each with ray-intersection and normals |
| **Camera** | Configurable view plane and resolution, built with a fluent **Builder** pattern |
| **Lighting** | Ambient, Directional, Point, and Spot lights with the **Phong** reflectance model (diffuse + specular) |
| **Shadows** | Hard shadows via shadow rays, plus **soft shadows** from area lights |
| **Reflection & refraction** | Mirror surfaces and transparent glass with recursive secondary rays and partial shadows |
| **Anti-aliasing** | Multiple jittered rays per pixel for smooth, crisp edges |
| **Super-sampling engine** | One reusable **beam sampler** ("blackboard") powering both anti-aliasing and soft shadows |
| **Multithreading** | Parallel rendering via raw threads or parallel streams for dramatic speedups |
| **Acceleration** | Conservative Bounding Regions (CBR) and a Bounding Volume Hierarchy (BVH) for fast scenes with many objects |
| **Scene loading** | Optional JSON scene parser — describe scenes in data, not code |

---

## 🖼️ Gallery

### Anti-Aliasing
Several rays are cast through different points inside each pixel and averaged, removing the jagged "staircase" edges of single-ray rendering.

| Without anti-aliasing | With anti-aliasing |
|:---:|:---:|
| <img src="images/antialiasing_without.png" width="400"> | <img src="images/antialiasing_with.png" width="400"> |

### Soft Shadows
Area lights cast many shadow rays, producing realistic soft penumbrae instead of hard, knife-edge shadows.

| Hard shadows | Soft shadows |
|:---:|:---:|
| <img src="images/zenGardenHard.png" width="400"> | <img src="images/zenGardenSoft.png" width="400"> |

### Reflection & Refraction
Recursive rays bring mirror reflections and transparent, light-bending glass to life.

| Crystal Gallery | Refraction & shadows |
|:---:|:---:|
| <img src="images/crystalGallery.png" width="400"> | <img src="images/refractionShadow.png" width="400"> |

### Mesh Rendering
A classic Utah Teapot, rendered triangle-by-triangle.

<p align="center">
  <img src="images/teapot1.png" alt="Utah Teapot" width="50%">
</p>

> 💡 All images above are produced by the test suite and saved to the `images/` folder.

---

## 🏗️ Architecture

The renderer follows a clean, layered design where each package has a single, well-defined responsibility:

```
        ┌──────────────┐
        │    scene     │   Scene = geometries + lights + background
        └──────┬───────┘
               │
   ┌───────────┼────────────┐
   │           │            │
┌──▼───┐  ┌────▼─────┐  ┌───▼──────┐
│lighting│ │geometries│  │ renderer │
│       │ │ api+impl │  │ Camera + │
│Phong, │ │ shapes,  │  │RayTracer │
│lights │ │ BVH/AABB │  │ + beams  │
└──┬────┘ └────┬─────┘  └───┬──────┘
   │           │            │
   └───────────┼────────────┘
               │
        ┌──────▼───────┐
        │  primitives  │   Point, Vector, Ray, Color, Material…
        └──────────────┘
```

- **`primitives`** — the mathematical foundation: vectors, points, rays, colors, materials, and an axis-aligned bounding box (AABB).
- **`geometries`** — split into `api` (the `Intersectable` / `Geometry` abstractions) and `impl` (concrete shapes + a `Geometries` composite), respecting the **Dependency Inversion Principle**.
- **`lighting`** — light sources and the Phong shading model.
- **`renderer`** — the `Camera` (Builder pattern), the `SimpleRayTracer` color pipeline, the `BeamSampler` super-sampling engine, and multithreading support.
- **`scene`** — bundles everything a renderer needs into one object.
- **`parser`** — optional JSON-to-scene loader, kept fully decoupled from the rendering core.

---

## 📂 Project Structure

```
src/
├── primitives/     Point, Vector, Ray, Color, Material, Util, AABB, Double3
├── geometries/
│   ├── api/        Intersectable, Geometry (abstractions)
│   └── impl/       Sphere, Plane, Triangle, Polygon, Tube, Cylinder, Geometries
├── renderer/       Camera, SimpleRayTracer, BeamSampler, ImageWriter, PixelManager
├── lighting/       AmbientLight, Directional/Point/Spot lights
├── scene/          Scene
└── parser/         JSON scene loading

unittests/          JUnit 5 tests mirroring the src/ packages
json/               Example scenes in JSON
images/             Rendered output images
```

---

## 🚀 Getting Started

### Requirements
- **Java 25** (JDK)
- **JUnit 5** and **org.json** (resolved automatically from the local Maven repository — see `.idea/libraries/`)
- **IntelliJ IDEA** recommended (this is a plain IntelliJ project — no Maven/Gradle build file)

### Run in IntelliJ
1. Open the project folder in IntelliJ IDEA.
2. Make sure the Project SDK is set to **Java 25**.
3. Right-click any test class in `unittests/` (e.g. `renderer/PisaTests`) and choose **Run**.
4. Rendered images appear in the `images/` folder.

### Quick regression check
`src/test/Main.java` is a lightweight entry point that exercises the core pipeline — run it and confirm it prints **no lines starting with `ERROR`**.

> ℹ️ This is intentionally a dependency-light, IDE-driven project (per course requirements), so there is no `mvn`/`gradle` command. Rendering is driven entirely through the JUnit test classes, each of which sets up a scene, configures a camera, and writes an image.

---

## 🔬 How It Works

At its heart, ray tracing simulates light **in reverse** — instead of following photons from lights into the eye, we shoot rays *from the camera through each pixel* into the scene and ask: *what does this ray see?*

For every pixel:

1. **Cast a ray** from the camera through the pixel on the view plane.
2. **Find the closest intersection** with any geometry in the scene.
3. **Compute the color** at that point:
   - the surface's own emission,
   - **ambient** light,
   - **local** lighting (Phong diffuse + specular) for each light source — checking shadows along the way,
   - **global** effects via recursive rays for **reflection** and **transparency**.
4. **Write the color** to the output image.

### Super-sampling
Rather than one ray per pixel, the **`BeamSampler`** generates a beam of rays distributed over a target area (using grid, stochastic, or **jittered** patterns) and averages the results. This single, reusable engine powers both **anti-aliasing** (rays spread across the pixel) and **soft shadows** (shadow rays spread across an area light).

### Going fast
- **Multithreading** splits the image across CPU cores (raw threads with a `PixelManager` mutex, or parallel streams).
- **CBR / BVH** wrap geometries in bounding boxes so the tracer can cheaply skip rays that can't possibly hit an object — essential for scenes with hundreds or thousands of shapes.

---

## 🧭 Design Principles

This project is as much a software-engineering exercise as a graphics one. The codebase deliberately applies:

- **Immutability** — primitives and geometries are immutable; every operation returns a *new* object.
- **No setters / no getters** (except where explicitly justified) — favouring expressive, intention-revealing APIs.
- **Design patterns** — Builder (`Camera`), Composite (`Geometries`), Template Method / NVI (`Intersectable`), Factory (scene parsing).
- **DRY & SRP** — one beam-sampling engine for all super-sampling effects; parsing kept out of the rendering core.
- **TDD** — features are defined, documented, tested, then implemented, with EP/BVA-driven JUnit 5 tests.
- **Safe floating-point math** — comparisons go through `Util.isZero` / `Util.alignZero`, never raw `==` against zero.

---

## 🗺️ Roadmap

The project is built in tagged stages:

| Stage | Topic |
|---|---|
| `PR01` | Primitives & geometric shapes |
| `PR02` | Unit tests + normals (TDD) |
| `PR03` | Ray–geometry intersections + `Geometries` composite |
| `PR04` | Camera (Builder) + ray construction |
| `PR05` | Rendering pipeline + Color + Ambient light + Scene |
| `PR06` | Emission + Material |
| `PR07` | Light sources + Phong shading |
| `PR08` | Shadows, reflection, transparency, partial shadows |
| **`MP01`** | Super-sampling (anti-aliasing + soft shadows) & multithreading |
| **`MP02`** | Acceleration (CBR / BVH) & performance |

---

<p align="center"><sub>Built with ☕ Java and a lot of linear algebra · ISE5786_9749_5354</sub></p>
