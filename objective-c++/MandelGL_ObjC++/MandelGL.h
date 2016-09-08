#import <Cocoa/Cocoa.h>
#include "Mandel.hpp"

@interface MandelGL : NSOpenGLView{
	double deltax, deltay;
	bool ready;
	NSTimer* timer;
}

-(NSTimer*)createTimer;
-(void)idle;
-(void)translateX:(double)x y:(double) y;
-(void)savePicture;

@end
