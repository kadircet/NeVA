#include "file.h"

#include <fstream>
#include <sstream>

void ReadFile(const std::string& file_name, std::string* buffer) {
  std::ifstream file(file_name.c_str(), std::ios::in);
  if (file.is_open()) {
    std::stringstream stream;
    stream << file.rdbuf();
    file.close();
    *buffer = stream.str();
  }
}
