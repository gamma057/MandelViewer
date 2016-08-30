#include "Color.hpp"

Color operator +(const Color& c1, const Color& c2){
	return Color{c1.r+c2.r, c1.g+c2.g, c1.b+c2.b, (c1.a+c2.a)/2};
}
Color operator -(const Color& c1, const Color& c2){
	return Color{c1.r-c2.r, c1.g-c2.g, c1.b-c2.b, (c1.a+c2.a)/2};
}
Color operator *(const Color& c1, double k){
	return Color{c1.r*k, c1.g*k, c1.b*k, c1.a};
}
Color operator *(double k, const Color& c2){
	return Color{k*c2.r, k*c2.g, k*c2.b, c2.a};
}
Color operator /(const Color& c1, double k){	
	return Color{c1.r/k, c1.g/k, c1.b/k, c1.a};
}

Color& operator +=(Color& c1, const Color& c2){
	c1.r += c2.r;
	c1.g += c2.g;
	c1.b += c2.b;
	return c1;
}
Color& operator -=(Color& c1, const Color& c2){
	c1.r -= c2.r;
	c1.g -= c2.g;
	c1.b -= c2.b;
	return c1;
}
Color& operator *=(Color& c1, double k){
	c1.r *= k;
	c1.g *= k;
	c1.b *= k;
	return c1;
}
Color& operator /=(Color& c1, double k){
	c1.r /= k;
	c1.g /= k;
	c1.b /= k;
	return c1;
}

Color fromHSVA(double h, double s, double v, double a){
	double r = v;
	double g = v;
	double b = v;
	if(s <= 0.0) return Color{0.0, 0.0, 0.0, a};
	h *= 6.0;
	const int i = static_cast<int>(h);
	const double f = h-i;
	switch(i){
		case 0:
			g *= 1-s*(1-f);
			b *= 1-s;
			break;
		case 1:
			r *= 1-s*f;
			b *= 1-s;
			break;
		case 2:
			r *= 1-s;
			b *= 1-s*(1-f);
			break;
		case 3:
			r *= 1-s;
			g *= 1-s*f;
			break;
		case 4:
			r *= 1-s*(1-f);
			g *= 1-s;
			break;
		case 5:
			g *= 1-s;
			b *= 1-s*f;
			break;
	}
	return Color{r, g, b, a};
}

Color d_rgba(double r, double g, double b, double a){
	return Color{r, g, b, a};
}
Color i_rgba(uint r, uint g, uint b, double a){
	return Color{r/256.0, g/256.0, b/256.0, a};
}
Color d_hsva(double h, double s, double v, double a){
	return fromHSVA(h, s, v, a);
}
Color i_hsva(uint h, uint s, uint v, double a){
	return fromHSVA(h/360.0, a/360.0, v/360.0, a);
}

Color color(ColorType type, double param){
	if(param >= 1.0){
		return BLACK;
	}else{
		switch(type){
			case ColorType::SUNSET:
				return d_hsva(param, 0.6, 1.0);
				break;
			case ColorType::RAINBOW:
				return d_hsva(6*param-floor(6*param), 0.5, 0.9+0.1*cos(24*M_PI*param));
				break;
			case ColorType::LEAF:
				return d_hsva(0.3, 0.3+0.3*sin(12*M_PI*param), 0.7+0.3*cos(16*M_PI*param));
				break;
			case ColorType::CRYSTAL:
				return d_hsva(0.55, 0.3+0.3*sin(12*M_PI*param), 0.7+0.3*cos(16*M_PI*param));
				break;
			case ColorType::TWILIGHT:
				auto f = [=](uint n){return static_cast<uint>(param*256*n)%256;};
				return i_rgba(f(7), f(5), f(3));
				break;
		}
	}
}