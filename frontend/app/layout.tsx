import "./globals.css";
import { ReactNode } from "react";
import Providers from "../components/Providers";

export const metadata = {
  title: "Prompt Lab",
  description: "Prompt-Lab para pesquisa em engenharia de prompt"
};

type RootLayoutProps = {
  children: ReactNode;
};

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="pt-BR">
      <body className="min-h-screen bg-slate-100">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
