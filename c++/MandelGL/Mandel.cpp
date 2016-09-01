#include <GLUT/GLUT.h>
#include "Mandel.hpp"

Mandel::Mandel(int width, int height, double xmin, double xmax, double ymin, double ymax, uint nmax, ColorType type): width(width), height(height), xmin(xmin), xmax(xmax), ymin(ymin), ymax(ymax), nmax(nmax), res(2), broad(false), type(type), list(1){
	map = std::vector<std::vector<double>>(width);
	for(auto& row : map){
		row = std::vector<double>(height);
	}
	setWeight(0.54);
}

const int Mandel::getWidth() noexcept{
	return broad? 0.5*width: width;
}
const int Mandel::getHeight() noexcept{
	return broad? 0.5*height: height;
}
const bool Mandel::isBroad() noexcept{
	return broad;
}

void Mandel::resize(int width, int height){
	this->width = width;
	this->height = height;
	map.resize(width);
	for(auto& row : map){
		row.resize(height);
	}
}

void Mandel::calc(double deltax, double deltay, double scale, double iter) noexcept{
	const double ratio = xmax-xmin < ymax-ymin? (xmax-xmin)/width: (ymax-ymin)/height;
	const double nxmin = (xmax+xmin)/2-scale*(xmax-xmin)/2+ratio*deltax;
	const double nxmax = (xmax+xmin)/2+scale*(xmax-xmin)/2+ratio*deltax;
	const double nymin = (ymax+ymin)/2-scale*(ymax-ymin)/2+ratio*deltay;
	const double nymax = (ymax+ymin)/2+scale*(ymax-ymin)/2+ratio*deltay;
	xmin = nxmin;
	xmax = nxmax;
	ymin = nymin;
	ymax = nymax;
	nmax = static_cast<int>(nmax*iter);
	const int dist = std::min(width, height);
	double c, d;
	for(int i = 0; i < width; i += res){
		for(int j = 0; j < height; j += res){
			c = (xmin+xmax)/2+(xmax-xmin)*(i-width/2)/dist;
			d = (ymin+ymax)/2+(ymax-ymin)*(j-height/2)/dist;
			map[i][j] = mandel(c, d);
		}
	}
}

void Mandel::draw() noexcept{
	glClear(GL_COLOR_BUFFER_BIT);
	if(glIsList(list) == GL_TRUE) glDeleteLists(list, 1);
	glNewList(list, GL_COMPILE_AND_EXECUTE);
	glBegin(GL_QUADS);
	const double dx = 2.0/getWidth();
	const double dy = 2.0/getHeight();
	double x1, y1, x2, y2;
	Color col;
	for(int i = 0; i < width; i += res){
		for(int j = 0; j < height; j += res){
			if(broad){
				x1 = -2.0+i*dx;
				y1 = -2.0+j*dy;
			}else{
				x1 = -1.0+i*dx;
				y1 = -1.0+j*dy;
			}
			x2 = x1+res*dx;
			y2 = y1+res*dy;
			col = blur(i, j);
			glColor3d(col.r, col.g, col.b);
			glVertex2d(x1, y2);
			glVertex2d(x1, y1);
			glVertex2d(x2, y1);
			glVertex2d(x2, y2);
		}
	}
	glEnd();
	glEndList();
	glFlush();
}

void Mandel::redraw() noexcept{
	glClear(GL_COLOR_BUFFER_BIT);
	glCallList(list);
	glFlush();
}

Color Mandel::blur(const int i, const int j) noexcept{
	if(i < res || i >= width-res || j < res || j >= height-res){
		return color(type, map[i][j]);
	}else{
		Color col = BLACK;
		for(int ii = 0; ii < 3; ii++){
			for(int jj = 0; jj < 3; jj++){
				col += weight[ii][jj]*color(type, map[i+res*(ii-1)][j+res*(jj-1)]);
			}
		}
		return col;
	}
}

void Mandel::setBroad(const bool broad) noexcept{
	if(this->broad == broad) return;
	this->broad = broad;
	const int owidth = width;
	const int oheight = height;
	if(broad){
		resize(2.0*owidth, 2.0*oheight);
	}else{
		resize(0.5*owidth, 0.5*oheight);
	}
}

void Mandel::setResolution(Resolution res) noexcept{
	switch(res){
		case Resolution::HIGH:
			this->res = 1;
			setWeight(1.0);
			break;
		case Resolution::MEDIUM:
			this->res = 2;
			setWeight(0.54);
			break;
		case Resolution::LOW:
			this->res = 3;
			setWeight(0.48);
			break;
		default:
			break;
	}
}

void Mandel::setWeight(const double sigma) noexcept{
	double sum = 0.0;
	for(int i = 0; i < 3; i++){
		for(int j = 0; j < 3; j++){
			sum += weight[i][j] = exp(-((i-1)*(i-1)+(j-1)*(j-1))/(2*sigma*sigma));
		}
	}
	for(int i = 0; i < 3; i++){
		for(int j = 0; j < 3; j++){
			weight[i][j] /= sum;
		}
	}
}

const double Mandel::mandel(const double c, const double d) noexcept{
	double x = 0.0, y = 0.0;
	double u, v;
	unsigned int n;
	for(n = 0; n < nmax; n++){
		u = x*x-y*y+c;
		v = 2*x*y+d;
		if(u*u+v*v > 4.0) break;
		x = u;
		y = v;
	}
	return static_cast<double>(n)/nmax;
}