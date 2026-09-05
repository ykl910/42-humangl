# humangl

A small Java/OpenGL 4.1 skeletal-animation project for 42. The fixed-function matrix API is not used: projection, view, model transforms, and the matrix stack are implemented in `src/main/java/com/humangl/math` and sent to the shader as uniforms.

## Requirements

- Linux or macOS with an OpenGL 4.1 capable context
- A JDK 21+ and the LWJGL jars — `make` downloads both if they are missing
  (into `~/goinfre/humangl-toolchain` when it exists, otherwise `~/.cache`),
  so no sudo and no preinstalled Maven are required.

## Run

```sh
make run
```

`make build` compiles only, `make info` prints the detected toolchain, `make clean`
removes the build output and `make fclean` also removes the downloaded toolchain.
The `pom.xml` is kept in sync for Maven/IntelliJ users; the native classifier is
selected by an OS-activated profile.

Controls: `A`/`S`/`D`/`F` set slow/normal/fast/faster animation speed, `SPACE` triggers one jump when standing still (jump input is ignored while walking), `R` triggers a torso spin in one direction and `T` in the opposite direction, `E` pauses/resumes, `ESC` quits. The humanoid is animated continuously; speed zero leaves it standing.

## Architecture

`Main` handles input and the frame loop, `Renderer` stores the current camera matrix, and `Humanoid` builds the animated character from a shared cube mesh.

### Main Flow

                    Humanoid.draw()
                          │
                          ▼
                    MatrixStack
                 "Where/how to transform?"
                          │
                          ▼
                    Body parts
              torso / head / arms / legs
                          │
                          │  each body part
                          │  uses a transformation
                          ▼
                     Cube.draw()
                          │
                          │
                 Cube geometry
                 "What vertices?"
                          │
                          ▼
                   ShaderProgram
                          │
                          ▼
                     GPU / OpenGL
                    /           \
                   ▼             ▼
            Vertex Shader   Fragment Shader
            "Where?"        "What color?"
                   \             /
                    ▼           ▼
                        Screen

The humanoid stays hierarchical under the hood, but the rendering path is intentionally simple: every visible part comes from one cube mesh and is positioned with stacked transforms.
