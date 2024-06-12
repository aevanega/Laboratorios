package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        LocalDate today = LocalDate.now();
        // Crear 3 clientes
        Cliente cliente1 = new Cliente(1, "Cliente 1", null, "RFC1", "1234567890", today);
        Cliente cliente2 = new Cliente(2, "Cliente 2", null, "RFC2", "0987654321", today);
        Cliente cliente3 = new Cliente(3, "Cliente 3", null, "RFC3", "1122334455", today);

        List<Cliente> clientes = List.of(cliente1, cliente2, cliente3);

        // Leer el archivo de cuentas y asignar cuentas a clientes
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/cuentas.txt"));
            lines.forEach(line -> {
                Cuenta cuenta = parseCuenta(line);
                if (cuenta != null) {
                    int clienteNumero = obtenerClienteNumero(line);
                    clientes.stream()
                            .filter(cliente -> cliente.getNumero() == clienteNumero)
                            .findFirst()
                            .ifPresent(cliente -> cliente.agregarCuenta(cuenta));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Validar que cada cliente tenga sus respectivas cuentas
        clientes.forEach(cliente -> {
            System.out.println("Cuentas del " + cliente.getNombre() + ":");
            for (Cuenta cuenta : cliente.obtenerCuentas()) {
                System.out.println(cuenta);
            }
        });
    }

    private static Cuenta parseCuenta(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // Verificar que la línea tiene al menos la longitud mínima esperada
        if (line.length() < 2) {
            return null;
        }

        String tipoCuenta = line.substring(0, 2);
        String cleanedLine = line.substring(2).replaceAll("[\\[\\]]", "");
        String[] parts = cleanedLine.split(",\\s*");

        // Verificar que hay suficientes partes en la línea
        if (parts.length < 5) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            Integer numero = Integer.parseInt(parts[0].trim());
            LocalDate fechaApertura = LocalDate.parse(parts[1].trim(),formatter);
            Double saldo = Double.parseDouble(parts[2].trim());
            Double interes = Double.parseDouble(parts[3].trim());
            Integer clienteNumero = Integer.parseInt(parts[4].trim());

            Cuenta cuenta;
            if (tipoCuenta.equals("CA")) {
                cuenta = new CuentaDeAhorro(numero, saldo, interes);
            } else {
                cuenta = new CuentaDeCheque(numero, saldo, interes);
            }
            cuenta.setFechaApertura(fechaApertura);
            cuenta.setNumero(clienteNumero);
            return cuenta;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int obtenerClienteNumero(String line) {
        String[] parts = line.split("\\[|,|\\]");
        return Integer.parseInt(parts[5].trim());
    }
}