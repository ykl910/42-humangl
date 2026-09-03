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

The project intentionally keeps the scene small: every body-part method draws exactly one unit cube at the origin of its current matrix. Parent transforms are pushed and popped around the hierarchy, so changing a segment length moves its child automatically.
