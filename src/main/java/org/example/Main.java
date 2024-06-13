package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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

        // Conexión a la base de datos y extracción de cuentas
        String url = "jdbc:mysql://localhost:3306/practica12";
        String user = "root";
        String password = "1234"; // Reemplazar con la contraseña de tu base de datos


        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM cuentas")) {

            while (resultSet.next()) {
                int numero = resultSet.getInt("numero");
                String fecha = resultSet.getString("fecha");
                double saldo = resultSet.getDouble("saldo");
                double interes = resultSet.getDouble("interes");
                int clienteNumero = resultSet.getInt("cliente");
                String tipoCuenta = resultSet.getString("tipoCuenta");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate fechaApertura = LocalDate.parse(fecha, formatter);

                Cuenta cuenta;
                if ("CA".equals(tipoCuenta)) {
                    cuenta = new CuentaDeAhorro(numero, saldo, interes);
                } else {
                    cuenta = new CuentaDeCheque(numero, saldo, interes);
                }
                cuenta.setFechaApertura(fechaApertura);
                cuenta.setNumero(clienteNumero);

                // Asignar cuenta al cliente correspondiente
                clientes.parallelStream()
                        .filter(cliente -> cliente.getNumero() == clienteNumero)
                        .findFirst()
                        .ifPresent(cliente -> cliente.agregarCuenta(cuenta));
            }
        } catch (Exception e) {
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

}