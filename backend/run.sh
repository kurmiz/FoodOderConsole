#!/bin/bash

echo "Starting FoodieExpress Backend..."
echo ""

if [ ! -d "out" ]; then
    echo "Compiled classes not found. Please run compile.sh first."
    exit 1
fi

java -cp out com.foodieexpress.FoodieExpressApp
