# humangl

A small Java/OpenGL 4.1 skeletal-animation project for 42. The fixed-function matrix API is not used: projection, view, model transforms, and the matrix stack are implemented in `src/main/java/com/humangl/math` and sent to the shader as uniforms.

## Requirements

- Java 21+
- Maven 3.9+
- macOS with an OpenGL 4.1 capable context

## Run

```sh
make run
```

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
