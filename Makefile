
MAIN    := com.humangl.Main
LWJGL   := 3.3.4
JDK_VER := 21
SRC     := src/main/java
RES     := src/main/resources
OUT     := target
LIB     := $(OUT)/lib

# uname, Adoptium and LWJGL spell architectures differently: normalise once.
ARCH := $(shell uname -m | sed 's/x86_64/x64/; s/arm64/aarch64/')
ifeq ($(shell uname -s),Darwin)
  PLAT := macos
  OS   := mac
  JVM  := -XstartOnFirstThread   # GLFW must own thread 0 on macOS, fatal to omit
  STRIP := 3                     # mac tarballs bury the JDK under <root>/Contents/Home
else
  PLAT := linux
  OS   := linux
  JVM  :=
  STRIP := 1
endif
# LWJGL suffixes the classifier on arm64 only: natives-linux vs natives-linux-arm64.
NATIVES := natives-$(PLAT)$(if $(filter aarch64,$(ARCH)),-arm64)

# A JDK is 200 MB+ and 42's home quota is small, so cache it in goinfre.
CACHE ?= $(firstword $(wildcard $(HOME)/goinfre) $(HOME)/.cache)/humangl-toolchain

# Reuse the system javac if it is recent enough, else use the cached JDK. The
# leading 0 keeps the test valid when no javac exists at all (empty -> "0").
SYS := $(shell command -v javac 2>/dev/null)
VER := $(if $(SYS),$(shell $(SYS) -version 2>&1 | sed -n 's/javac \([0-9]*\).*/\1/p'))
ifeq ($(shell [ "0$(VER)" -ge $(JDK_VER) ] && echo ok),ok)
  JDK :=$(patsubst %/bin/,%,$(dir $(SYS)))
  NEED_JDK :=
else
  JDK := $(CACHE)/jdk$(JDK_VER)
  NEED_JDK := $(JDK)/bin/javac
endif

# Each module needs its API jar *and* the matching natives jar.
JARS := $(foreach m,lwjgl lwjgl-glfw lwjgl-opengl,\
          $(LIB)/$m-$(LWJGL).jar $(LIB)/$m-$(LWJGL)-$(NATIVES).jar)
SRCS := $(shell find $(SRC) -name '*.java')

SHELL := /bin/bash   # process substitution, used to filter one stderr line below

# Recipes are silent; `make V=1` echoes the real commands instead.
Q := $(if $(V),,@)

.DEFAULT_GOAL := run
.PHONY: run build info clean fclean re
.DELETE_ON_ERROR:   # never leave a half-downloaded jar looking like a valid one

run: build
	$(Q)$(JDK)/bin/java $(JVM) -cp "$(OUT)/classes:$(LIB)/*" $(MAIN) \
	  2> >(grep -v "libdecor" >&2)   # benign Wayland decoration warning

build: $(OUT)/.built

# javac has no 1-source-1-object mapping, so one stamp guards the whole compile
# and keeps a second `make` a no-op.
$(OUT)/.built: $(SRCS) $(JARS) $(NEED_JDK)
	@echo "  compile $(words $(SRCS)) sources"
	$(Q)$(JDK)/bin/javac --release $(JDK_VER) -encoding UTF-8 -d $(OUT)/classes -cp "$(LIB)/*" $(SRCS)
	@cp -R $(RES)/. $(OUT)/classes/   # shaders are loaded via getResourceAsStream
	@touch $@

# lwjgl-glfw-3.3.4-natives-linux.jar -> org/lwjgl/lwjgl-glfw/3.3.4/<same name>
$(LIB)/%.jar:
	@mkdir -p $(LIB) && echo "  fetch $*.jar"
	@a="$*"; curl -fsSL --retry 3 -o $@ \
		  "https://repo1.maven.org/maven2/org/lwjgl/$${a%%-$(LWJGL)*}/$(LWJGL)/$*.jar"

# Named by its real path (not $(NEED_JDK)) so the rule stays valid when unused.
$(CACHE)/jdk$(JDK_VER)/bin/javac:
	@echo "  install Temurin $(JDK_VER) -> $(JDK)"
	@mkdir -p $(JDK) && curl -fsSL -o $(CACHE)/jdk.tgz \
		  "https://api.adoptium.net/v3/binary/latest/$(JDK_VER)/ga/$(OS)/$(ARCH)/jdk/hotspot/normal/eclipse"
	@tar xzf $(CACHE)/jdk.tgz --strip-components=$(STRIP) -C $(JDK) && rm $(CACHE)/jdk.tgz


clean:
	@rm -rf $(OUT)

fclean: clean
	@rm -rf $(CACHE)

re: clean run
