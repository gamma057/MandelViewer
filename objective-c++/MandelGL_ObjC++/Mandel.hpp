#ifndef Mandel_hpp
#define Mandel_hpp

#include <vector>
#include "Color.hpp"

constexpr int init_width = 600;
constexpr int init_height = 600;
constexpr double init_xmin = -2.1;
constexpr double init_xmax = 0.5;
constexpr double init_ymin = -1.3;
constexpr double init_ymax = 1.3;
constexpr uint init_nmax = 300;
constexpr int init_res = 2;
constexpr double init_sigma[] = {1.0, 0.54, 0.48};
constexpr ColorType init_type = ColorType::SUNSET;

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
	
	Mandel();
	Mandel(int width, int height, double xmin, double xmax, double ymin, double ymax, uint nmax, ColorType type);
	
	void resize(int width, int height);
	const int getWidth() noexcept;
	const int getHeight() noexcept;
	const bool isBroad() noexcept;
	void calc(double deltax = 0.0, double deltay = 0.0, double scale = 1.0, double iter = 1.0) noexcept;
	void draw() noexcept;
	void redraw() noexcept;
	Color blur(const int i, const int j) noexcept;
	void changeBroad() noexcept;
	void setResolution(Resolution res) noexcept;
	void setWeight(const double sigma) noexcept;
	const double mandel(const double c, const double d) noexcept;
};

#endif /* Mandel_hpp */
