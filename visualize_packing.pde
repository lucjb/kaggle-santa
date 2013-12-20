
/*

  Visualizes a solution to the Packing Santa's Sleigh competition:
  http://www.kaggle.com/c/packing-santas-sleigh
  
  This should work with a few thousand presents.
  
  You can move the camera vertically with the cursor keys (UP and DOWN).

*/


int[][] presents;
int zCamera;

void setup()
{
  size(1000, 1000, P3D);
  zCamera = 0;

  // Parsing the submission file:
  String[] lines  = loadStrings("../fastxycompact.csv");
  presents = new int[lines.length][7];
  for (int i = 1; i < lines.length; i++) { // the first line is the CSV header
    int[] fields = int(lines[i].replace("\"","").split(","));
    int id = fields[0];
    int x1 = fields[1];
    int x2 = fields[22];
    int y1 = fields[2];
    int y2 = fields[23];
    int z1 = fields[3];
    int z2 = fields[24];
    
    int[] present = {id,
                     x1 - 1,      y1 - 1,      z1 - 1,
                     x2 - x1 + 1, y2 - y1 + 1, z2 - z1 + 1};
    presents[i] = present;
    
    // move the camera up the sleigh
    if (z2 > zCamera) {
      zCamera = z2;
    }
  }
}

void draw()
{
  lights();
  background(0);

  camera(1000, 1000, zCamera + (mouseY - height/2)*3,
         0.0, 0.0, zCamera, // centerX, centerY, centerZ
         0.0, 0.0, -1.0); // upX, upY, upZ

  for (int i=0; i < presents.length; i++) {
    int[] present = presents[i];
    int id = present[0];
    int x  = present[1];
    int y  = present[2];
    int z  = present[3];
    int dx = present[4];
    int dy = present[5];
    int dz = present[6];
    pushMatrix();
    rotateZ(-radians(mouseX/2));
    translate(-width/2, -height/2, 0);
    translate(x+dx/2, y+dy/2, z+dz/2);
    box(dx, dy, dz);
    popMatrix();
  }
}

// Move the camera in z direction with cursor keys
void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) {
      zCamera += 250;
    } else if (keyCode == DOWN) {
      zCamera -= 250;
    }
  }
} 

