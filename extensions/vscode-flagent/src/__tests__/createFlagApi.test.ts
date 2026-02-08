import { createFlagApi } from "../api/createFlagApi";
import { mockRequest } from "../__mocks__/axios";

describe("createFlagApi", () => {
  beforeEach(() => {
    mockRequest.mockClear();
  });

  it("findFlags returns flags from API", async () => {
    mockRequest.mockResolvedValue({
      data: [{ id: 1, key: "test_flag", description: "Test", enabled: true }],
    });
    const api = createFlagApi("http://localhost:18000");
    const { data } = await api.findFlags(100);
    expect(data).toHaveLength(1);
    expect(data![0].key).toBe("test_flag");
    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({
        method: "GET",
        url: expect.stringContaining("/flags"),
      })
    );
  });

  it("adds X-API-Key header when apiKey provided", async () => {
    mockRequest.mockResolvedValue({ data: [] });
    const api = createFlagApi("http://localhost:18000", "secret");
    await api.findFlags(100);
    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({
        headers: expect.objectContaining({ "X-API-Key": "secret" }),
      })
    );
  });

  it("getFlag fetches single flag", async () => {
    mockRequest.mockResolvedValue({
      data: {
        id: 42,
        key: "my_flag",
        description: "Desc",
        enabled: false,
      },
    });
    const api = createFlagApi("http://localhost:18000");
    const { data } = await api.getFlag(42);
    expect(data!.key).toBe("my_flag");
    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({
        url: expect.stringMatching(/\/flags\/42$/),
      })
    );
  });

  it("uses correct basePath", async () => {
    mockRequest.mockResolvedValue({ data: [] });
    const api = createFlagApi("https://api.example.com");
    await api.findFlags(100);
    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({
        url: expect.stringContaining("https://api.example.com/api/v1/flags"),
      })
    );
  });
});
