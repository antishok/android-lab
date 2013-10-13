uniform mat4 uMVPMatrix;	
void main()
{
   gl_Position = uMVPMatrix * vec4(0, 0, 0, 1.0);
   gl_PointSize = 5.0;
}
