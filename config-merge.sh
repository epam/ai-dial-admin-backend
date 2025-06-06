#!/bin/bash

# Initialize variables
OUTPUT_FILE="aidial.config.json"
INPUT_FILES=()

# Function to display usage
usage() {
    echo "Usage: $0 [--output <output_file>] <file1.json> <file2.json> [<file3.json> ...]"
    echo "  --output: Specify the output file (default: aidial.config.json)"
    exit 1
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --output)
            if [[ -z "$2" || "$2" == --* ]]; then
                echo "Error: --output requires a file name"
                usage
            fi
            OUTPUT_FILE="$2"
            shift 2
            ;;
        --help)
            usage
            ;;
        -*)
            echo "Unknown option: $1"
            usage
            ;;
        *)
            INPUT_FILES+=("$1")
            shift
            ;;
    esac
done

# Check if at least one input file is provided
if [ ${#INPUT_FILES[@]} -eq 0 ]; then
    echo "Error: At least one input file is required"
    usage
fi

# Run the jq command with all provided JSON files and save the output
jq -rs '
def deployment_fields: ["displayName", "description", "userRoles", "iconUrl", "displayVersion"];

def filter_fields(fields):
  with_entries(select(.key as $k | fields | index($k)))
;

def process_config_item:
  {
    roles,
    applications: (.applications // {}) | map_values(filter_fields(deployment_fields)),
    models: (.models // {}) | map_values(filter_fields(deployment_fields))
  }
;

reduce .[] as $item ({}; . * ($item | process_config_item))
' "${INPUT_FILES[@]}" > "$OUTPUT_FILE"

echo "Merged JSON saved to $OUTPUT_FILE"