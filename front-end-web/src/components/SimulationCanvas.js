import React, { useRef, useEffect, useState } from 'react';
import { Stage, Layer, Rect, Label, Tag, Text, Circle, RegularPolygon } from 'react-konva';
import Grid from './Grid';
import AnimatedArrow from './AnimatedArrow';
import { Menu, MenuItem } from '@mui/material';

const SimulationCanvas = ({ buildings = [], roads = [], sendCommand }) => {
  const containerRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 0, height: 0 });
  // context-menu state
  const [ctxMenu, setCtxMenu] = useState(null);
  const [ctxBuilding, setCtxBuilding] = useState(null);
  
  
  // Tooltip state: visible flag, position, and text to display.
  const [tooltip, setTooltip] = useState({ visible: false, x: 0, y: 0, text: '' });
  
  const gridSize = 25; 

  useEffect(() => {
    const updateDimensions = () => {
      if (containerRef.current) {
        setDimensions({
          width: containerRef.current.offsetWidth,
          height: containerRef.current.offsetHeight,
        });
      }
    };

    updateDimensions();
    window.addEventListener('resize', updateDimensions);
    return () => window.removeEventListener('resize', updateDimensions);
  }, []);

  // Determine the fill color based on building type.
  const getFillColor = (b) => {
    if (b.disposeAmount !== undefined) {
      return '#8FOOFF';         
    } else if (b.drones !== undefined) {
      return '#DAA520';         
    } else if (b.mine !== undefined) {
      return 'brown';
    } else if (b.stores !== undefined) {
      return 'green';
    } else {
      return 'blue';
    }
  };

  // Build tooltip text from the building object
  const buildTooltipText = (b) => {
    const name = b.name || "Unnamed";
    const [x, y] = b.coordinate
      ? b.coordinate
      : [b.x ?? 0, b.y ?? 0];
  
    // Determine building type
    const type = b.type
      || (b.mine   !== undefined ? "mine"
      : b.stores !== undefined ? "storage"
      : b.disposeAmount !== undefined ? "waste disposal"
      : b.drones !== undefined ? "drone port"
      : "factory");
  
    // Start with basics
    const lines = [
      `${name}`,
      `Coordinates: (${x}, ${y})`,
      `Type: ${type}`,
    ];
  
    if (type !== "waste disposal") {
      // only for non–waste disposal
      lines.push(
        `Req Policy: ${b.requestPolicy}`,
        `Src Policy: ${b.sourcePolicy}`,
        `Sources: ${
          Array.isArray(b.sources) && b.sources.length > 0
            ? b.sources.map(src => src[0]).join(", ")
            : "None"
        }`,
        `Requests: ${
          Array.isArray(b.requests) && b.requests.length > 0
            ? `${b.requests.length} request(s)`
            : "None"
        }`,
        `Inventory: ${
          b.inventory && Object.keys(b.inventory).length > 0
            ? Object.entries(b.inventory)
                .map(([k, v]) => `${k}: ${v}`)
                .join(", ")
            : "Empty"
        }`
      );
    } else {
      // for waste disposal only
      lines.push(
        `Dispose Amount: ${b.disposeAmount ?? "N/A"}`,
        `Capacity:      ${b.capacity ?? "N/A"}`,
        `Interval:      ${b.disposeInterval ?? "N/A"}`,
        `Waste Types:   ${
          Array.isArray(b.wasteTypes) && b.wasteTypes.length > 0
            ? b.wasteTypes.join(", ")
            : "None"
        }`,
        `Current Amount:   ${b.currentAmount ?? "N/A"}`,
        `Predicted Amount: ${b.predictedAmount ?? "N/A"}`
      );
    }
  
    // Storage
    if (b.stores !== undefined) {
      lines.push(
        `Stores:   ${b.stores}`,
        `Capacity: ${b.capacity ?? "N/A"}`,
        `Remain:   ${b.remain ?? "N/A"}`,
        `Deliveries: ${
          Array.isArray(b.deliveries) && b.deliveries.length > 0
            ? `${b.deliveries.length} delivery(ies)`
            : "None"
        }`
      );
    }
  
    // Drone port
    if (Array.isArray(b.drones)) {
      const total = b.drones.length;
      const inUse = b.drones.filter(d => d.inUse).length;
      lines.push(`Drones (in use/total): ${inUse}/${total}`);
      b.drones.forEach((d, i) => {
        lines.push(
          `· Drone ${i+1}: ${
            d.inUse ? `busy (${d.timeleft} left)` : "idle"
          }`
        );
      });
    }
  
    return lines.join("\n");
  };
  
  const handleContextMenu = (e, building) => {
    e.evt.preventDefault();
    const stage = e.target.getStage();
    const pointer = stage.getPointerPosition();
    setCtxBuilding(building);
    setCtxMenu({
      mouseX: pointer.x,
      mouseY: pointer.y,
    });
  };

  const handleCloseMenu = () => {
    setCtxMenu(null);
    setCtxBuilding(null);
  };

  const handleAddDrone = () => {
    if (ctxBuilding && Array.isArray(ctxBuilding.drones)) {
      sendCommand({ command: `add_drone at '${ctxBuilding.name}'` });
    }
    handleCloseMenu();
  };

  const handleRemove = () => {
    if (ctxBuilding) {
      console.log(ctxBuilding.name);
      sendCommand({ command: `remove '${ctxBuilding.name}'` });
    }
    handleCloseMenu();
  };
  

  return (    
    <div className="canvas-area" ref={containerRef}>
      <Stage width={dimensions.width} height={dimensions.height}>
        {/* Background Grid Layer */}
        <Layer>
          <Grid canvasWidth={dimensions.width} canvasHeight={dimensions.height} gridSize={gridSize} />
        </Layer>

        {/* Roads Layer rendered as arrows */}
        <Layer>
          {roads.map((r, idx) => {
            // Calculate the center of the grid cell for the road coordinate.
            const startX = r.coordinate[0] * gridSize + dimensions.width / 2 + gridSize / 2;
            const startY = r.coordinate[1] * gridSize + dimensions.height / 2 + gridSize / 2;
            // Calculate the end position from the center.
            const endX = startX + r.direction[0] * gridSize;
            const endY = startY + r.direction[1] * gridSize;

            return (
              <AnimatedArrow
                key={idx}
                startX={startX}
                startY={startY}
                endX={endX}
                endY={endY}
              />
            );
          })}
        </Layer>
         {/* Deliveries Layer */}
        <Layer>
          {buildings.flatMap((b) =>
            (b.deliveries || []).map((d) => {
              // Calculate the center of the grid cell for the delivery coordinate.
              const cx = d.coordinate[0] * gridSize + dimensions.width / 2 + gridSize / 2;
              const cy = d.coordinate[1] * gridSize + dimensions.height / 2 + gridSize / 2;

              return (
                <Circle
                  key={`${b.name}-${d.requestID}`}
                  x={cx}
                  y={cy}
                  radius={gridSize * 0.2}
                  fill="orange"
                  opacity={0.8}
                  onMouseEnter={(e) => {
                    const stage = e.target.getStage();
                    const pos = stage.getPointerPosition();
                    setTooltip({
                      visible: true,
                      x: pos.x + 5,
                      y: pos.y + 5,
                      text: `Req ${d.requestID}\nTo: ${d.requester}\nETA: ${d.timeleft}`,
                    });
                    stage.container().style.cursor = 'pointer';
                  }}
                  onMouseLeave={(e) => {
                    setTooltip({ visible: false, x: 0, y: 0, text: '' });
                    e.target.getStage().container().style.cursor = 'default';
                  }}
                />
              );
            })
          )}
        </Layer>

        {/* Buildings Layer */}
        <Layer>
          {buildings.map((b) => (
            <Rect
              key={b.name}
              // Adjust coordinates to center the origin
              x={(b.coordinate ? b.coordinate[0] : b.x) * gridSize + dimensions.width / 2}
              y={(b.coordinate ? b.coordinate[1] : b.y) * gridSize + dimensions.height / 2}
              width={gridSize}
              height={gridSize}
              fill={getFillColor(b)}
              stroke="black"
              onContextMenu={e => handleContextMenu(e, b)}
              onMouseEnter={(e) => {
                const stage = e.target.getStage();
                const pointerPos = stage.getPointerPosition();
                const tooltipText = buildTooltipText(b);
                setTooltip({
                  visible: true,
                  x: pointerPos.x + 10,
                  y: pointerPos.y + 10,
                  text: tooltipText,
                });
                stage.container().style.cursor = 'pointer';
              }}
              onMouseLeave={(e) => {
                setTooltip({ visible: false, x: 0, y: 0, text: '' });
                e.target.getStage().container().style.cursor = 'default';
              }}
            />
          ))}
        </Layer>
         {/* Drone Layer */}     
        <Layer>
          {buildings.flatMap((b) =>
            Array.isArray(b.drones)
              ? b.drones.map((d, i) => {
                  const [wx = 0, wy = 0] = d.coordinate || [];
                  const [sx = wx, sy = wy] = d.source || [];
                  const x = wx * gridSize + dimensions.width / 2 + gridSize / 2;
                  const y = wy * gridSize + dimensions.height / 2 + gridSize / 2;
                  const vecX = wx - sx;
                  const vecY = wy - sy;
                  const rotation = (Math.atan2(vecY, vecX) * 180) / Math.PI;
                  // tooltip text
                  const tooltipText = [
                    `Drone ${i + 1}`,
                    `Status: ${d.inUse ? 'busy' : 'idle'}`,
                    d.inUse ? `ETA: ${d.timeleft}` : '',
                  ]
                    .filter(Boolean)
                    .join('\n');

                  return (
                    <RegularPolygon
                      key={`${b.name}-drone-${i}`}
                      x={x}
                      y={y}
                      sides={5}   
                      radius={gridSize * 0.35}
                      fill="red"
                      opacity={0.9}
                      rotation={rotation}
                      onMouseEnter={(e) => {
                        const stage = e.target.getStage();
                        const pos = stage.getPointerPosition() || { x: 0, y: 0 };
                        setTooltip({
                          visible: true,
                          x: pos.x + 5,
                          y: pos.y + 5,
                          text: tooltipText,
                        });
                        stage.container().style.cursor = 'pointer';
                      }}
                      onMouseLeave={(e) => {
                        setTooltip((t) => ({ ...t, visible: false }));
                        e.target.getStage().container().style.cursor = 'default';
                      }}
                    />
                  );
                })
              : []
          )}
        </Layer>

        {/* Tooltip Layer */}
        {tooltip.visible && (
          <Layer>
            <Label x={tooltip.x} y={tooltip.y}>
              <Tag
                fill="black"
                shadowColor="black"
                shadowBlur={10}
                opacity={0.75}
              />
              <Text
                text={tooltip.text}
                fontSize={16}
                padding={5}
                fill="white"
              />
            </Label>
          </Layer>
        )}
        
      </Stage>
      {/* MUI context menu */}
      <Menu
        open={Boolean(ctxMenu)}
        onClose={handleCloseMenu}
        anchorReference="anchorPosition"
        anchorPosition={
          ctxMenu
            ? { top: ctxMenu.mouseY, left: ctxMenu.mouseX }
            : undefined
        }
      >
        {ctxBuilding?.drones && (
          <MenuItem onClick={handleAddDrone}>
            Add Drone
          </MenuItem>
        )}
        <MenuItem onClick={handleRemove} sx={{ color: 'error.main' }}>
          Remove Building
        </MenuItem>
      </Menu>
    </div>
  );
};
export default SimulationCanvas;
