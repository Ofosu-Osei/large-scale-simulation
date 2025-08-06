import React from 'react';
import { Line } from 'react-konva';

function Grid({ canvasWidth, canvasHeight, gridSize }) {
  const lines = [];
  const halfWidth = canvasWidth / 2;
  const halfHeight = canvasHeight / 2;

  // Draw vertical grid lines
  for (let i = -Math.ceil(halfWidth / gridSize); i <= Math.ceil(halfWidth / gridSize); i++) {
    lines.push(
      <Line
        key={`v-${i}`}
        points={[i * gridSize + halfWidth, 0, i * gridSize + halfWidth, canvasHeight]}
        stroke="gray"
        strokeWidth={0.5}
      />
    );
  }

  // Draw horizontal grid lines
  for (let j = -Math.ceil(halfHeight / gridSize); j <= Math.ceil(halfHeight / gridSize); j++) {
    lines.push(
      <Line
        key={`h-${j}`}
        points={[0, j * gridSize + halfHeight, canvasWidth, j * gridSize + halfHeight]}
        stroke="gray"
        strokeWidth={0.5}
      />
    );
  }

  return <>{lines}</>;
}

export default Grid;