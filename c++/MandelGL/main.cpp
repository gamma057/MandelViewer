#include <thread>
#include <future>
#include <GLUT/glut.h>
#include "Mandel.hpp"
#include "Color.hpp"

using namespace std::chrono_literals;

constexpr int init_width = 600;
constexpr int init_height = 600;
constexpr double init_xmin = -2.1;
constexpr double init_xmax = 0.5;
constexpr double init_ymin = -1.3;
constexpr double init_ymax = 1.3;
constexpr uint init_nmax = 300;
constexpr ColorType init_type = ColorType::SUNSET;

Mandel mandel(init_width, init_height, init_xmin, init_xmax, init_ymin, init_ymax, init_nmax, init_type);
double deltax, deltay;
bool dragging;
auto func = [](const double dx = 0.0, const double dy = 0.0, const double sc = 1.0, const double it = 1.0){mandel.calc(dx, -dy, sc, it);};
auto fut = std::async(std::launch::deferred, func);

void display(){
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
	glViewport(0, 0, mandel.getWidth(), mandel.getHeight());
	fut = std::async(std::launch::async, func);
	mandel.draw();
}

void idle(){
	if(fut.wait_for(0s) == std::future_status::ready && !dragging){
		glLoadIdentity();
		mandel.draw();
		deltax = deltay = 0.0;
	}
}

void resize(int width, int height){
	if(mandel.isBroad()){
		mandel.resize(2*width, 2*height);
	}else{
		mandel.resize(width, height);
	}
}

void mouse(int button, int state, int x, int y){
	if(button != GLUT_LEFT_BUTTON) return;
	if(state == GLUT_DOWN){
		deltax = x;
		deltay = y;
		dragging = true;
	}else if(state == GLUT_UP){
		deltax -= x;
		deltay -= y;
		fut = std::async(std::launch::async, func, deltax, deltay);
		dragging = false;
	}
}

void drag(int x, int y){
	const double dx = 2.0*(x-deltax)/mandel.getWidth();
	const double dy = 2.0*(y-deltay)/mandel.getHeight();
	glLoadIdentity();
	glTranslated(dx, -dy, 0.0);
	mandel.redraw();
	dragging = true;
}

void mouseWheel(int wheel_number, int direction, int x, int y){
}

void keyboard(unsigned char key, int x, int y){
	switch(key){
		case 'r':
			mandel = Mandel(init_width, init_height, init_xmin, init_xmax, init_ymin, init_ymax, init_nmax, init_type);
			glutReshapeWindow(init_width, init_height);
			display();
			break;
		case 'q':
			exit(EXIT_SUCCESS);
			break;
		case '+':
			fut = std::async(std::launch::async, func, 0.0, 0.0, 1.0, 1.1);
			break;
		case '-':
			fut = std::async(std::launch::async, func, 0.0, 0.0, 1.0, 1/1.1);
			break;
		case '1':
			mandel.type = ColorType::SUNSET;
			mandel.draw();
			break;
		case '2':
			mandel.type = ColorType::RAINBOW;
			mandel.draw();
			break;
		case '3':
			mandel.type = ColorType::LEAF;
			mandel.draw();
			break;
		case '4':
			mandel.type = ColorType::CRYSTAL;
			mandel.draw();
			break;
		case '5':
			mandel.type = ColorType::TWILIGHT;
			mandel.draw();
			break;
		case 'h':
			mandel.setResolution(Resolution::HIGH);
			fut = std::async(std::launch::async, func);
			break;
		case 'm':
			mandel.setResolution(Resolution::MEDIUM);
			fut = std::async(std::launch::async, func);
			break;
		case 'l':
			mandel.setResolution(Resolution::LOW);
			fut = std::async(std::launch::async, func);
			break;
		case 'b':
			mandel.setBroad(!mandel.isBroad());
			if(mandel.isBroad()){
				fut = std::async(std::launch::async, func, 0.0, 0.0, 2.0);
			}else{
				fut = std::async(std::launch::async, func, 0.0, 0.0, 0.5);
			}
			break;
		default:
			break;
	}
}

void spkeyboard(int key, int x, int y){
	if(glutGetModifiers() & GLUT_ACTIVE_SHIFT){
		switch(key){
			case GLUT_KEY_UP:
				glScaled(1.2, 1.2, 1.0);
				mandel.redraw();
				fut = std::async(std::launch::async, func, 0.0, 0.0, 1/1.2, 1.03);
				break;
			case GLUT_KEY_DOWN:
				glScaled(1/1.2, 1/1.2, 1.0);
				mandel.redraw();
				fut = std::async(std::launch::async, func, 0.0, 0.0, 1.2, 1/1.03);
				break;
			default:
				break;
		}
		return;
	}
	double ddx = 0.0, ddy = 0.0;
	switch(key){
		case GLUT_KEY_RIGHT:
			ddx = 40.0;
			break;
		case GLUT_KEY_LEFT:
			ddx = -40.0;
			break;
		case GLUT_KEY_UP:
			ddy = -40.0;
			break;
		case GLUT_KEY_DOWN:
			ddy = 40.0;
			break;
		default:
			return;
	}
	deltax += ddx;
	deltay += ddy;
	const double dx = -2.0*ddx/mandel.getWidth();
	const double dy = -2.0*ddy/mandel.getHeight();
	glTranslated(dx, -dy, 0.0);
	mandel.redraw();
	fut = std::async(std::launch::async, func, ddx, ddy);
}

void initGL(){
	glClearColor(1.0, 1.0, 1.0, 0.0);
}

int main(int argc, char* argv[]){
	glutInitWindowSize(init_width, init_height);
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_RGBA);
	glutCreateWindow("Mandelbrot");
	glutDisplayFunc(display);
	glutIdleFunc(idle);
	glutReshapeFunc(resize);
	glutDisplayFunc(display);
	glutMouseFunc(mouse);
	glutMotionFunc(drag);
	//glutMouseWheelFunc(mouseWheel);
	glutKeyboardFunc(keyboard);
	glutSpecialFunc(spkeyboard);
	initGL();
	glutMainLoop();
	return 0;
}
