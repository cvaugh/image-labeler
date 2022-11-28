# Image Labeler

A graphical tool for quickly labeling a large dataset of images. Labels are stored in a tab-separated values (`.tsv`) file
with the following format:

| Path | Label |
|------|-------|
|`/home/dataset/000000.jpg`|`FACE`|
|`/home/dataset/000001.jpg`|`NOT_FACE`|
|...|...|

## Usage
### Keyboard
- **O**: Load a directory of images
- **S**: Save labels
- **Left/right arrow keys**: Move back or forward one image, respectively
- **Up/down arrow keys**: Move back or forward ten images, respectively
- **1**: Label the current image as `FACE`
- **2**: Label the current image as `NOT_FACE`
- **3**: Label the current image as `AMBIGUOUS`
- **0**: Remove the label from the current image

### Mouse

- Left-click an image to label all images between the current image and the clicked image (inclusive) as `FACE`
- Right-click an image to label all images between the current image and the clicked image (inclusive) as `NOT_FACE`
- Middle-click an image to label all images between the current image and the clicked image (inclusive) as `AMBIGUOUS`
- Click with mouse buttons 4 or 5 to skip to the clicked image without changing any labels
