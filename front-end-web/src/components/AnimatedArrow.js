import React, { useEffect, useRef } from 'react';
import { Arrow } from 'react-konva';
import Konva from 'konva';

function AnimatedArrow({ startX, startY, endX, endY }) {
  const arrowRef = useRef(null);

  useEffect(() => {
    const tween = new Konva.Tween({
      node: arrowRef.current,
      duration: 1, // Duration of one cycle of the animation
      dashOffset: -20, // Moves the dashes forward
      easing: Konva.Easings.Linear,
      onFinish: () => tween.reset().play(), // Loop the animation
    });

    tween.play();

    return () => {
      tween.destroy(); // Cleanup on unmount
    };
  }, []);

  return (
    <Arrow
      ref={arrowRef}
      points={[startX, startY, endX, endY]}
      pointerLength={10}
      pointerWidth={10}
      fill="gray"
      stroke="gray"
      strokeWidth={2}
      dash={[10, 10]} // Dashed line pattern
    />
  );
}

export default AnimatedArrow;