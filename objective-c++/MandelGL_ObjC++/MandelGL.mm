#import <GLUT/GLUT.h>
#import "MandelGL.h"
#include <thread>
#include <future>
#include "Mandel.hpp"

using namespace std::chrono_literals;

Mandel mandel;
auto calc = [](const double dx = 0.0, const double dy = 0.0, const double sc = 1.0, const double it = 1.0){mandel.calc(dx, dy, sc, it);};
auto fut = std::async(std::launch::deferred, calc);

@implementation MandelGL

-(void)awakeFromNib{
	deltax = deltay = 0.0;
	ready = true;
	timer = [self createTimer];
}

-(void)drawRect:(NSRect)dirtyRect{
	[super drawRect:dirtyRect];
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
	glViewport(0, 0, mandel.getWidth(), mandel.getHeight());
	fut = std::async(std::launch::async, calc);
	mandel.draw();
}

-(void)reshape{
	ready = false;
	auto size = [self bounds].size;
	const int width = size.width;
	const int height = size.height;
	if(mandel.isBroad()){
		mandel.resize(2*width, 2*height);
	}else{
		mandel.resize(width, height);
	}
	ready = true;
}

-(void)mouseDown:(NSEvent*)e{
	ready = false;
	auto location = [e locationInWindow];
	deltax = location.x;
	deltay = location.y;
}

-(void)mouseUp:(NSEvent*)e{
	auto location = [e locationInWindow];
	deltax -= location.x;
	deltay -= location.y;
	fut = std::async(std::launch::async, calc, deltax, deltay);
	ready = true;
}

-(void)mouseDragged:(NSEvent*)e{
	ready = false;
	auto location = [e locationInWindow];
	const double dx = 2.0*(location.x-deltax)/mandel.getWidth();
	const double dy = 2.0*(location.y-deltay)/mandel.getHeight();
	glLoadIdentity();
	glTranslated(dx, dy, 0.0);
	mandel.redraw();
}

-(void)scrollWheel:(NSEvent*)e{
	const double scale = pow(1.01, [e deltaY]);
	glScaled(scale, scale, 1.0);
	mandel.redraw();
	fut = std::async(std::launch::async, calc, 0.0, 0.0, 1/scale, pow(scale, 0.16));
}

-(void)keyDown:(NSEvent*)e{
	auto key = [e keyCode];
	auto flags = [e modifierFlags];
	if(flags & NSCommandKeyMask){
		switch(key){
			case 126: //up
				glScaled(1.2, 1.2, 1.0);
				mandel.redraw();
				fut = std::async(std::launch::async, calc, 0.0, 0.0, 1/1.2, 1.03);
				break;
			case 125: //down
				glScaled(1/1.2, 1/1.2, 1.0);
				mandel.redraw();
				fut = std::async(std::launch::async, calc, 0.0, 0.0, 1.2, 1/1.03);
				break;
			default:
				break;
		}
		return;
	}
	switch(key){
		case 124: //right
			[self translate:40.0 y:0.0];
			return;
		case 123: //left
			[self translate:-40.0 y:0.0];
			return;
		case 126: //up
			[self translate:0.0 y:40.0];
			return;
		case 125: //down
			[self translate:0.0 y:-40.0];
			return;
		case 41:  //'+'
			if(flags & NSShiftKeyMask){
				fut = std::async(std::launch::async, calc, 0.0, 0.0, 1.0, 1.1);
			}
			break;
		case 27:  //'-'
			fut = std::async(std::launch::async, calc, 0.0, 0.0, 1.0, 1/1.1);
			break;
		case 18:  //'1'
			mandel.type = ColorType::SUNSET;
			mandel.draw();
			break;
		case 19:  //'2'
			mandel.type = ColorType::RAINBOW;
			mandel.draw();
			break;
		case 20:  //'3'
			mandel.type = ColorType::LEAF;
			mandel.draw();
			break;
		case 21:  //'4'
			mandel.type = ColorType::CRYSTAL;
			mandel.draw();
			break;
		case 23:  //'5'
			mandel.type = ColorType::TWILIGHT;
			mandel.draw();
			break;
		case 4:   //'h'
			mandel.setResolution(Resolution::HIGH);
			fut = std::async(std::launch::async, calc);
			break;
		case 46:  //'m'
			mandel.setResolution(Resolution::MEDIUM);
			fut = std::async(std::launch::async, calc);
			break;
		case 37:  //'l'
			mandel.setResolution(Resolution::LOW);
			fut = std::async(std::launch::async, calc);
			break;
		case 11:  //'b'
			mandel.setBroad(!mandel.isBroad());
			if(mandel.isBroad()){
				fut = std::async(std::launch::async, calc, 0.0, 0.0, 2.0);
			}else{
				fut = std::async(std::launch::async, calc, 0.0, 0.0, 0.5);
			}
			break;
		case 15:  //'r'
			mandel = Mandel();
			[self.window setContentSize:NSMakeSize(init_width, init_height)];
			[self update];
			break;
		default:
			break;
	}
}

-(BOOL)acceptsFirstResponder{
	return YES;
}

-(NSTimer*)createTimer{
	 return [NSTimer scheduledTimerWithTimeInterval:0.05
	 target:self
	 selector:@selector(idle)
	 userInfo:nil
	 repeats:NO];
}

-(void)idle{
	if(fut.wait_for(0s) == std::future_status::ready && ready){
		glLoadIdentity();
		mandel.draw();
		deltax = deltay = 0.0;
	}
	timer = [self createTimer];
}

-(void)translate:(double) x y:(double) y{
	deltax += x;
	deltay += y;
	const double dx = -2.0*x/mandel.getWidth();
	const double dy = -2.0*y/mandel.getHeight();
	glTranslated(dx, dy, 0.0);
	mandel.redraw();
	fut = std::async(std::launch::async, calc, x, y);
}

-(void)prepareOpenGL{
	glClearColor(1.0, 1.0, 1.0, 0.0);
}

@end
