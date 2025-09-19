import NextAuth, { DefaultSession } from "next-auth";

declare module "next-auth" {
  interface Session {
    id_token?: string;
    user?: DefaultSession["user"];
  }

  interface JWT {
    id_token?: string;
  }
}
