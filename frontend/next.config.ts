import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    serverActions: {
      // Default 1MB is too small for scanned tax documents.
      bodySizeLimit: "25mb",
    },
  },
};

export default nextConfig;
