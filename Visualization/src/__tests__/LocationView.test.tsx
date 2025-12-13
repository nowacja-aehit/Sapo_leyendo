import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor, fireEvent, cleanup } from "@testing-library/react";
import { LocationView } from "../components/dashboard/LocationView";
import type { Location, Zone, LocationType } from "../services/locationService";
import * as locationService from "../services/locationService";

const mockZones: Zone[] = [
  { id: 1, name: "STREFA-A", isSecure: false, isTemperatureControlled: false },
  { id: 2, name: "STREFA-B", isSecure: false, isTemperatureControlled: false },
];

const mockTypes: LocationType[] = [
  { id: 1, name: "PALLET" },
  { id: 2, name: "SHELF" },
];

const mockLocations: Location[] = [
  { id: 101, name: "A-01-01", zone: mockZones[0], locationType: mockTypes[0], status: "ACTIVE", barcode: "A0101" },
  { id: 102, name: "A-01-02", zone: mockZones[1], locationType: mockTypes[1], status: "ACTIVE", barcode: "A0102" },
];

describe("LocationView", () => {
  beforeEach(() => {
    vi.spyOn(locationService, "getAllLocations").mockResolvedValue(mockLocations);
    vi.spyOn(locationService, "getAllZones").mockResolvedValue(mockZones);
    vi.spyOn(locationService, "getAllLocationTypes").mockResolvedValue(mockTypes);
    vi.spyOn(locationService, "deleteLocation").mockResolvedValue(undefined as unknown as void);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    cleanup();
  });

  it("renderuje listę lokalizacji i statystyki", async () => {
    render(<LocationView />);

    await waitFor(() => {
      expect(screen.getByText("A-01-01")).toBeInTheDocument();
      expect(screen.getByText("A-01-02")).toBeInTheDocument();
    });

    expect(screen.getByText(mockLocations.length.toString())).toBeInTheDocument();
    expect(screen.getByText(mockZones.length.toString())).toBeInTheDocument();
    expect(screen.getByText(mockTypes.length.toString())).toBeInTheDocument();
  });

  it("usuwa lokalizację po kliknięciu ikony kosza", async () => {
    render(<LocationView />);

    await waitFor(() => {
      expect(screen.getByText("A-01-01")).toBeInTheDocument();
    });

    const deleteButtons = screen.getAllByLabelText(/Usuń/);
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(locationService.deleteLocation).toHaveBeenCalledTimes(1);
      expect(screen.queryByText("A-01-01")).not.toBeInTheDocument();
    });
  });
});
