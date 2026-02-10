# Compile all Java sources into build/
BUILD_DIR = build
LOX_SOURCES = lox/*.java
TOOL_SOURCES = tool/*.java

.PHONY: all clean run

all: $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(LOX_SOURCES) $(TOOL_SOURCES)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

# Default main class; override with: make run CLASS=tool.GenerateAST ARGS=lox
CLASS ?= lox.Lox

run: all
	java -cp $(BUILD_DIR) $(CLASS) $(ARGS)

clean:
	rm -rf $(BUILD_DIR)
