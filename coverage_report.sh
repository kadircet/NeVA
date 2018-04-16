coverage_report=$(cat bazel-kcov/index.json)
print_output='console.log("Covered: " + data["merged_files"][0]["covered"])'

echo $coverage_report $print_output | nodejs
