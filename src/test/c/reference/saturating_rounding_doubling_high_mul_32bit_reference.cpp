#include <gemmlowp.h>
#include <iostream>
#include <limits>
#include <random>

int main(int argc, char* argv[]) {
  std::random_device rand_dev;
  std::mt19937 generator(rand_dev());
  std::uniform_int_distribution<int32_t> distr(
      std::numeric_limits<int32_t>::min(), std::numeric_limits<int32_t>::max());
  int n_samples = 32;

  if (argc > 1) {
    n_samples = std::stoi(argv[1]);
  }

  for (unsigned int i = 0; i < n_samples; i++) {
    int32_t a = distr(generator);
    int32_t b = distr(generator);

    int32_t result = gemmlowp::SaturatingRoundingDoublingHighMul(a, b);

    std::cout <<  a << "," << b << "," << result << std::endl;
  }
}