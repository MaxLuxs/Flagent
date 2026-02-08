const mockRequest = jest.fn();
export default {
  defaults: { baseURL: "" },
  request: (...args: unknown[]) => mockRequest(...args),
  get: (...args: unknown[]) => mockRequest(...args),
  post: (...args: unknown[]) => mockRequest(...args),
};
export { mockRequest };
