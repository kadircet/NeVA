#include "gtest/gtest.h"

#include "hello-world.h"

TEST(GetHelloString, SanityTest) {
  EXPECT_EQ(GetHelloString(), "HELLO WORLD!");
}
