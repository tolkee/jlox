# Compile all Java sources into build/
BUILD_DIR = build
LOX_SOURCES = lox/*.java
TOOL_SOURCES = tool/*.java

.PHONY: all clean run

all: $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(LOX_SOURCES) $(TOOL_SOURCES)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

run: all
	java -cp $(BUILD_DIR) lox.Lox $(ARGS)

clean:
	rm -rf $(BUILD_DIR)
