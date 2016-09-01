#ifndef Color_hpp
#define Color_hpp

#include <iostream>
#include <cmath>

struct Color{
	double r, g, b, a;
};

constexpr Color BLACK{0.0, 0.0, 0.0, 1.0};
constexpr Color WHITE{1.0, 1.0, 1.0, 1.0};
constexpr Color RED{1.0, 0.0, 0.0, 1.0};
constexpr Color GREEN{0.0, 1.0, 0.0, 1.0};
constexpr Color BLUE{0.0, 0.0, 1.0, 1.0};

Color operator +(const Color& c1, const Color& c2);
Color operator -(const Color& c1, const Color& c2);
Color operator *(const Color& c1, double k);
Color operator *(double k, const Color& c2);
Color operator /(const Color& c1, double k);

Color& operator +=(Color& c1, const Color& c2);
Color& operator -=(Color& c1, const Color& c2);
Color& operator *=(Color& c1, double k);
Color& operator /=(Color& c1, double k);

Color fromHSVA(double h, double s, double v, double a = 1.0);

Color d_rgba(double r, double g, double b, double a = 1.0);
Color i_rgba(uint r, uint g, uint b, double a = 1.0);
Color d_hsva(double h, double s, double v, double a = 1.0);
Color i_hsva(uint h, uint s, uint v, double a = 1.0);

enum class ColorType{
	SUNSET,
	RAINBOW,
	LEAF,
	CRYSTAL,
	TWILIGHT
};

Color color(ColorType type, double param);

#endif /* Color_hpp */
