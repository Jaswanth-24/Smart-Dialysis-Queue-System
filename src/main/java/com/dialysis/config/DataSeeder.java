package com.dialysis.config;

import com.dialysis.entity.Center;
import com.dialysis.entity.Machine;
import com.dialysis.entity.Patient;
import com.dialysis.entity.User;
import com.dialysis.enums.MachineStatus;
import com.dialysis.enums.Role;
import com.dialysis.repository.CenterRepository;
import com.dialysis.repository.MachineRepository;
import com.dialysis.repository.PatientRepository;
import com.dialysis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CenterRepository    centerRepository;
    private final UserRepository      userRepository;
    private final MachineRepository   machineRepository;
    private final PatientRepository   patientRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) {

        // ── 1. CENTER ──────────────────────────────────────────────────────
        Center center = centerRepository.save(
                Center.builder()
                        .name("Apollo Dialysis Center")
                        .city("Hyderabad")
                        .address("Banjara Hills, Road No. 12, Hyderabad - 500034")
                        .phone("+91-40-23456789")
                        .machineCount(4)
                        .isActive(true)
                        .build()
        );

        // ── 2. STAFF ───────────────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .center(center)
                .name("System Admin")
                .email("admin@dialysis.com")
                .password(bCryptPasswordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        User receptionist = userRepository.save(User.builder()
                .center(center)
                .name("Meena Kumari")
                .email("receptionist@dialysis.com")
                .password(bCryptPasswordEncoder.encode("password123"))
                .role(Role.RECEPTIONIST)
                .isActive(true)
                .build());

        User technician = userRepository.save(User.builder()
                .center(center)
                .name("Arjun Reddy")
                .email("technician@dialysis.com")
                .password(bCryptPasswordEncoder.encode("password123"))
                .role(Role.TECHNICIAN)
                .isActive(true)
                .build());

        User nurse = userRepository.save(User.builder()
                .center(center)
                .name("Sunita Rao")
                .email("nurse@dialysis.com")
                .password(bCryptPasswordEncoder.encode("password123"))
                .role(Role.NURSE)
                .isActive(true)
                .build());

        User doctor = userRepository.save(User.builder()
                .center(center)
                .name("Dr. Venkat Prasad")
                .email("doctor@dialysis.com")
                .password(bCryptPasswordEncoder.encode("password123"))
                .role(Role.DOCTOR)
                .isActive(true)
                .build());

        // ── 3. MACHINES ────────────────────────────────────────────────────
        Machine m1 = machineRepository.save(Machine.builder()
                .center(center)
                .machineNumber("M-01")
                .status(MachineStatus.AVAILABLE)
                .lastServiced(LocalDate.now().minusDays(10))
                .isActive(true)
                .build());

        Machine m2 = machineRepository.save(Machine.builder()
                .center(center)
                .machineNumber("M-02")
                .status(MachineStatus.AVAILABLE)
                .lastServiced(LocalDate.now().minusDays(5))
                .isActive(true)
                .build());

        Machine m3 = machineRepository.save(Machine.builder()
                .center(center)
                .machineNumber("M-03")
                .status(MachineStatus.AVAILABLE)
                .lastServiced(LocalDate.now().minusDays(2))
                .isActive(true)
                .build());

        Machine m4 = machineRepository.save(Machine.builder()
                .center(center)
                .machineNumber("M-04")
                .status(MachineStatus.MAINTENANCE)
                .lastServiced(LocalDate.now().minusDays(30))
                .isActive(false)
                .build());

        // ── 4. PATIENTS ────────────────────────────────────────────────────
        User patientUser1 = userRepository.save(User.builder()
                .center(center)
                .name("Sai Kumar")
                .email("sai@patient.com")
                .password(bCryptPasswordEncoder.encode("patient123"))
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        Patient patient1 = patientRepository.save(Patient.builder()
                .user(patientUser1)
                .center(center)
                .doctor(doctor)
                .dateOfBirth(LocalDate.of(1975, 3, 15))
                .bloodGroup("B+")
                .diagnosis("Chronic Kidney Disease Stage 5")
                .emergencyContactName("Lakshmi Kumar")
                .emergencyContactPhone("+91-9876543210")
                .build());

        User patientUser2 = userRepository.save(User.builder()
                .center(center)
                .name("Priya Reddy")
                .email("priya@patient.com")
                .password(bCryptPasswordEncoder.encode("patient123"))
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        Patient patient2 = patientRepository.save(Patient.builder()
                .user(patientUser2)
                .center(center)
                .doctor(doctor)
                .dateOfBirth(LocalDate.of(1982, 7, 22))
                .bloodGroup("O+")
                .diagnosis("End Stage Renal Disease")
                .emergencyContactName("Suresh Reddy")
                .emergencyContactPhone("+91-9876543211")
                .build());

        User patientUser3 = userRepository.save(User.builder()
                .center(center)
                .name("Ravi Sharma")
                .email("ravi@patient.com")
                .password(bCryptPasswordEncoder.encode("patient123"))
                .role(Role.PATIENT)
                .isActive(true)
                .build());

        Patient patient3 = patientRepository.save(Patient.builder()
                .user(patientUser3)
                .center(center)
                .doctor(null)  // no doctor assigned — tests optional doctor path
                .dateOfBirth(LocalDate.of(1968, 11, 5))
                .bloodGroup("A-")
                .diagnosis("Diabetic Nephropathy")
                .emergencyContactName("Kavita Sharma")
                .emergencyContactPhone("+91-9876543212")
                .build());

        // ── 5. PRINT SUMMARY ───────────────────────────────────────────────
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              DIALYSIS SEEDER — TEST DATA READY              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  CENTER                                                      ║");
        System.out.printf( "║  %-20s %s%n", "Apollo Center ID:", center.getId());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  STAFF — all passwords: password123                          ║");
        System.out.printf( "║  %-20s %s%n", "ADMIN:",        admin.getEmail()        + "  id:" + admin.getId());
        System.out.printf( "║  %-20s %s%n", "RECEPTIONIST:", receptionist.getEmail() + "  id:" + receptionist.getId());
        System.out.printf( "║  %-20s %s%n", "TECHNICIAN:",   technician.getEmail()   + "  id:" + technician.getId());
        System.out.printf( "║  %-20s %s%n", "NURSE:",        nurse.getEmail()        + "  id:" + nurse.getId());
        System.out.printf( "║  %-20s %s%n", "DOCTOR:",       doctor.getEmail()       + "  id:" + doctor.getId());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  MACHINES                                                    ║");
        System.out.printf( "║  %-6s AVAILABLE  id: %s%n", "M-01:", m1.getId());
        System.out.printf( "║  %-6s AVAILABLE  id: %s%n", "M-02:", m2.getId());
        System.out.printf( "║  %-6s AVAILABLE  id: %s%n", "M-03:", m3.getId());
        System.out.printf( "║  %-6s MAINTENANCE (inactive) id: %s%n", "M-04:", m4.getId());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  PATIENTS — all passwords: patient123                        ║");
        System.out.printf( "║  %-14s %s%n", "Sai Kumar:",   "id:" + patient1.getId() + "  email:" + patientUser1.getEmail());
        System.out.printf( "║  %-14s %s%n", "Priya Reddy:", "id:" + patient2.getId() + "  email:" + patientUser2.getEmail());
        System.out.printf( "║  %-14s %s%n", "Ravi Sharma:", "id:" + patient3.getId() + "  email:" + patientUser3.getEmail());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  QUICK TEST FLOW                                             ║");
        System.out.println("║  1. Login as receptionist → get token                        ║");
        System.out.println("║  2. POST /api/queue/check-in with any patientId above         ║");
        System.out.println("║     → machine auto-assigned (ASSIGNED status)                 ║");
        System.out.println("║  3. Login as technician → get token                          ║");
        System.out.println("║  4. POST /api/sessions/{queueId}/start                        ║");
        System.out.println("║     → status becomes IN_SESSION                               ║");
        System.out.println("║  5. PATCH /api/sessions/{sessionId}/complete                  ║");
        System.out.println("║     → machine goes to CLEANING for 20min                      ║");
        System.out.println("║  6. GET /api/public/live/{centerId} to see live queue          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\n");
    }
}