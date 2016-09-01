#ifndef Mandel_h
#define Mandel_h

#include <iostream>
#include <cmath>
#include <vector>
#include "Color.hpp"

enum class Resolution{
	HIGH,
	MEDIUM,
	LOW
};

class Mandel{
private:
	std::vector<std::vector<double>> map;
	double xmin, xmax, ymin, ymax;
	int width, height;
	uint nmax;
	int res;
	bool broad;
	double weight[3][3];
	uint list;
	
public:
	ColorType type;
	
	Mandel(int width, int height, double xmin, double xmax, double ymin, double ymax, uint nmax, ColorType type);
	Mandel& operator =(Mandel&& mandel) = default;
	
	void resize(int width, int height);
	const int getWidth() noexcept;
	const int getHeight() noexcept;
	const bool isBroad() noexcept;
	void calc(double deltax = 0.0, double deltay = 0.0, double scale = 1.0, double iter = 1.0) noexcept;
	void draw() noexcept;
	void redraw() noexcept;
	Color blur(const int i, const int j) noexcept;
	void setBroad(const bool broad) noexcept;
	void setResolution(Resolution res) noexcept;
	void setWeight(const double sigma) noexcept;
	const double mandel(const double c, const double d) noexcept;
};

#endif /* Mandel_h */
