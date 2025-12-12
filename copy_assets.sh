#!/bin/bash

# Script to copy images from Expo project to Android drawable folder
# Run this from the _charmings_android directory

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SOURCE_DIR="$SCRIPT_DIR/../assets/images"
DEST_DIR="$SCRIPT_DIR/app/src/main/res/drawable"
ASSETS_DIR="$SCRIPT_DIR/app/src/main/assets"

# Create destination directories
mkdir -p "$DEST_DIR"
mkdir -p "$ASSETS_DIR"

echo "Copying images from $SOURCE_DIR to $DEST_DIR"

# Copy and rename images (IMAGE_XX.jpg -> image_xx.jpg)
for i in $(seq -w 0 66); do
    SOURCE_FILE="$SOURCE_DIR/IMAGE_$i.jpg"
    DEST_FILE="$DEST_DIR/image_$i.jpg"
    
    if [ -f "$SOURCE_FILE" ]; then
        cp "$SOURCE_FILE" "$DEST_FILE"
        echo "Copied IMAGE_$i.jpg -> image_$i.jpg"
    else
        echo "Warning: $SOURCE_FILE not found"
    fi
done

# Copy background images
if [ -f "$SOURCE_DIR/bg.png" ]; then
    cp "$SOURCE_DIR/bg.png" "$DEST_DIR/bg.png"
    echo "Copied bg.png"
fi

if [ -f "$SOURCE_DIR/bg3.jpg" ]; then
    cp "$SOURCE_DIR/bg3.jpg" "$DEST_DIR/bg3.jpg"
    echo "Copied bg3.jpg"
fi

# Copy Lottie animation if exists
LOTTIE_SOURCE="$SCRIPT_DIR/../assets/animations/fireworks.json"
if [ -f "$LOTTIE_SOURCE" ]; then
    cp "$LOTTIE_SOURCE" "$ASSETS_DIR/fireworks.json"
    echo "Copied fireworks.json animation"
else
    echo "Warning: fireworks.json not found, creating placeholder"
    # Create a simple placeholder animation
    cat > "$ASSETS_DIR/fireworks.json" << 'EOF'
{"v":"5.5.7","fr":30,"ip":0,"op":60,"w":400,"h":400,"nm":"Fireworks","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"Star","sr":1,"ks":{"o":{"a":1,"k":[{"t":0,"s":[100]},{"t":30,"s":[0]}]},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":60,"s":[360]}]},"p":{"a":0,"k":[200,200]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":0,"s":[0,0]},{"t":15,"s":[100,100]},{"t":60,"s":[200,200]}]}},"shapes":[{"ty":"sr","sy":1,"d":1,"pt":{"a":0,"k":5},"p":{"a":0,"k":[0,0]},"r":{"a":0,"k":0},"ir":{"a":0,"k":20},"is":{"a":0,"k":0},"or":{"a":0,"k":50},"os":{"a":0,"k":0},"ix":1,"nm":"Star"},{"ty":"fl","c":{"a":0,"k":[1,0.8,0,1]},"o":{"a":0,"k":100},"r":1,"nm":"Fill"}],"ip":0,"op":60,"st":0}]}
EOF
fi

echo ""
echo "=== Asset copy complete ==="
echo "Images copied to: $DEST_DIR"
echo "Assets copied to: $ASSETS_DIR"
