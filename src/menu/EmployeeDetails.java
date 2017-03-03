package menu;

import entity.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

public class EmployeeDetails extends JFrame implements ActionListener, ItemListener, DocumentListener, WindowListener
{
    // decimal format for inactive currency text field
    private static final DecimalFormat format = new DecimalFormat("\u20ac ###,###,##0.00");
    // decimal format for active currency text field
    private static final DecimalFormat fieldFormat = new DecimalFormat("0.00");
    // display files in File Chooser only with extension .dat
    private FileNameExtensionFilter datfilter = new FileNameExtensionFilter("dat files (*.dat)", "dat");
    // holds true or false if any changes are made for text fields
    private boolean change = false;
    // holds true or false if any changes are made for file content
    boolean changesMade = false;
    private JMenuItem open, save, saveAs, create, modify, delete, firstItem, lastItem, nextItem, prevItem, searchById,
            searchBySurname, listAll, closeApp;
    private JButton first, previous, next, last, add, edit, deleteButton, displayAll, searchId, searchSurname,
            saveChange, cancelChange;
    private JComboBox<String> genderCombo, departmentCombo, fullTimeCombo;
    private JTextField idField, ppsField, surnameField, firstNameField, salaryField;
    private static EmployeeDetails frame = new EmployeeDetails();
    // font for labels, text fields and combo boxes
    Font font1 = new Font("SansSerif", Font.BOLD, 16);
    // holds automatically generated file name
    String generatedFileName;
    JTextField searchByIdField, searchBySurnameField;
    // gender combo box values
    String[] gender = { "", "M", "F" };
    // department combo box values
    String[] department = { "", "Administration", "Production", "Transport", "Management" };
    // full time combo box values
    String[] fullTime = { "", "Yes", "No" };

    Controller controller = new Controller();

    // initialize menu bar
    private JMenuBar menuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu, recordMenu, navigateMenu, closeMenu;

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        recordMenu = new JMenu("Records");
        recordMenu.setMnemonic(KeyEvent.VK_R);
        navigateMenu = new JMenu("Navigate");
        navigateMenu.setMnemonic(KeyEvent.VK_N);
        closeMenu = new JMenu("Exit");
        closeMenu.setMnemonic(KeyEvent.VK_E);

        menuBar.add(fileMenu);
        menuBar.add(recordMenu);
        menuBar.add(navigateMenu);
        menuBar.add(closeMenu);

        open = Extensions.createMenuItemWithMnemonicAndAccelerator("Open", KeyEvent.VK_0, ActionEvent.CTRL_MASK);
        save = Extensions.createMenuItemWithMnemonicAndAccelerator("Save", KeyEvent.VK_S, ActionEvent.CTRL_MASK);
        saveAs = Extensions.createMenuItemWithMnemonicAndAccelerator("Save As", KeyEvent.VK_F2, ActionEvent.CTRL_MASK);

        fileMenu.add(open).addActionListener(this);
        fileMenu.add(save).addActionListener(this);
        fileMenu.add(saveAs).addActionListener(this);

        create = Extensions.createMenuItemWithMnemonicAndAccelerator("Create new Record", KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        modify = Extensions.createMenuItemWithMnemonicAndAccelerator("Modify Record", KeyEvent.VK_E, ActionEvent.CTRL_MASK);

        recordMenu.add(create).addActionListener(this);
        recordMenu.add(modify).addActionListener(this);
        recordMenu.add(delete = new JMenuItem("Delete Record")).addActionListener(this);

        navigateMenu.add(firstItem = new JMenuItem("First"));
        firstItem.addActionListener(this);
        navigateMenu.add(prevItem = new JMenuItem("Previous"));
        prevItem.addActionListener(this);
        navigateMenu.add(nextItem = new JMenuItem("Next"));
        nextItem.addActionListener(this);
        navigateMenu.add(lastItem = new JMenuItem("Last"));
        lastItem.addActionListener(this);
        navigateMenu.addSeparator();
        navigateMenu.add(searchById = new JMenuItem("Search by ID")).addActionListener(this);
        navigateMenu.add(searchBySurname = new JMenuItem("Search by Surname")).addActionListener(this);
        navigateMenu.add(listAll = new JMenuItem("List all Records")).addActionListener(this);

        closeMenu.add(closeApp = new JMenuItem("Close")).addActionListener(this);
        closeApp.setMnemonic(KeyEvent.VK_F4);
        closeApp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));

        return menuBar;
    }// end menuBar

    // initialize search panel
    private JPanel searchPanel()
    {
        JPanel searchPanel = new JPanel(new MigLayout());

        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        searchPanel.add(new JLabel("Search by ID:"), "growx, pushx");
        searchPanel.add(searchByIdField = new JTextField(20), "width 200:200:200, growx, pushx");
        searchByIdField.addActionListener(this);
        searchByIdField.setDocument(new JTextFieldLimit(20));
        searchPanel.add(searchId = new JButton(new ImageIcon(
                        new ImageIcon("imgres.png").getImage().getScaledInstance(35, 20, java.awt.Image.SCALE_SMOOTH))),
                "width 35:35:35, height 20:20:20, growx, pushx, wrap");
        searchId.addActionListener(this);
        searchId.setToolTipText("Search Employee By ID");

        searchPanel.add(new JLabel("Search by Surname:"), "growx, pushx");
        searchPanel.add(searchBySurnameField = new JTextField(20), "width 200:200:200, growx, pushx");
        searchBySurnameField.addActionListener(this);
        searchBySurnameField.setDocument(new JTextFieldLimit(20));
        searchPanel.add(
                searchSurname = new JButton(new ImageIcon(new ImageIcon("imgres.png").getImage()
                        .getScaledInstance(35, 20, java.awt.Image.SCALE_SMOOTH))),
                "width 35:35:35, height 20:20:20, growx, pushx, wrap");
        searchSurname.addActionListener(this);
        searchSurname.setToolTipText("Search Employee By Surname");

        return searchPanel;
    }// end searchPanel

    // initialize navigation panel
    private JPanel navigPanel()
    {
        JPanel navigPanel = new JPanel();

        navigPanel.setBorder(BorderFactory.createTitledBorder("Navigate"));

        first = Extensions.createJButtonWithImageAndOther("first.png", "Display first Record", this);
        previous = Extensions.createJButtonWithImageAndOther("previous.png", "Display previous Record", this);
        next = Extensions.createJButtonWithImageAndOther("next.png", "Display next Record", this);
        last = Extensions.createJButtonWithImageAndOther("last.png", "Display last Record", this);

        navigPanel.add(first);
        navigPanel.add(previous);
        navigPanel.add(next);
        navigPanel.add(last);

        return navigPanel;
    }

    private JPanel buttonPanel()
    {
        JPanel buttonPanel = new JPanel();

        add = Extensions.createJButtonWithActionListenerAndToolTip("Add Record", "Add new Employee Record", this);
        edit = Extensions.createJButtonWithActionListenerAndToolTip("Edit Record", "Edit current Employee", this);
        deleteButton = Extensions.createJButtonWithActionListenerAndToolTip("Delete Record", "Delete current Employee", this);
        displayAll = Extensions.createJButtonWithActionListenerAndToolTip("List all Records", "List all Registered Employees", this);

        buttonPanel.add(add, "growx, pushx");
        buttonPanel.add(edit, "growx, pushx");
        buttonPanel.add(deleteButton, "growx, pushx, wrap");
        buttonPanel.add(displayAll, "growx, pushx");

        return buttonPanel;
    }

    // initialize main/details panel
    private JPanel detailsPanel()
    {
        JPanel empDetails = new JPanel(new MigLayout());
        JPanel buttonPanel = new JPanel();
        JTextField field;

        empDetails.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        empDetails.add(new JLabel("ID:"), "growx, pushx");
        empDetails.add(idField = new JTextField(20), "growx, pushx, wrap");
        idField.setEditable(false);

        empDetails.add(new JLabel("PPS Number:"), "growx, pushx");
        empDetails.add(ppsField = new JTextField(20), "growx, pushx, wrap");

        empDetails.add(new JLabel("Surname:"), "growx, pushx");
        empDetails.add(surnameField = new JTextField(20), "growx, pushx, wrap");

        empDetails.add(new JLabel("First Name:"), "growx, pushx");
        empDetails.add(firstNameField = new JTextField(20), "growx, pushx, wrap");

        empDetails.add(new JLabel("Gender:"), "growx, pushx");
        empDetails.add(genderCombo = new JComboBox<>(gender), "growx, pushx, wrap");

        empDetails.add(new JLabel("Department:"), "growx, pushx");
        empDetails.add(departmentCombo = new JComboBox<>(department), "growx, pushx, wrap");

        empDetails.add(new JLabel("Salary:"), "growx, pushx");
        empDetails.add(salaryField = new JTextField(20), "growx, pushx, wrap");

        empDetails.add(new JLabel("Full Time:"), "growx, pushx");
        empDetails.add(fullTimeCombo = new JComboBox<>(fullTime), "growx, pushx, wrap");

        buttonPanel.add(saveChange = new JButton("Save"));
        saveChange.addActionListener(this);
        saveChange.setVisible(false);
        saveChange.setToolTipText("Save changes");
        buttonPanel.add(cancelChange = new JButton("Cancel"));
        cancelChange.addActionListener(this);
        cancelChange.setVisible(false);
        cancelChange.setToolTipText("Cancel edit");

        empDetails.add(buttonPanel, "span 2,growx, pushx,wrap");

        // loop through panel components and add listeners and format
        for (int i = 0; i < empDetails.getComponentCount(); i++)
        {
            empDetails.getComponent(i).setFont(font1);
            if (empDetails.getComponent(i) instanceof JTextField)
            {
                field = (JTextField) empDetails.getComponent(i);
                field.setEditable(false);
                if (field == ppsField)
                    field.setDocument(new JTextFieldLimit(9));
                else
                    field.setDocument(new JTextFieldLimit(20));
                field.getDocument().addDocumentListener(this);
            } // end if
            else if (empDetails.getComponent(i) instanceof JComboBox)
            {
                empDetails.getComponent(i).setBackground(Color.WHITE);
                empDetails.getComponent(i).setEnabled(false);
                ((JComboBox<String>) empDetails.getComponent(i)).addItemListener(this);
                ((JComboBox<String>) empDetails.getComponent(i)).setRenderer(new DefaultListCellRenderer()
                {
                    // set foregroung to combo boxes
                    public void paint(Graphics g)
                    {
                        setForeground(new Color(65, 65, 65));
                        super.paint(g);
                    }// end paint
                });
            } // end else if
        } // end for
        return empDetails;
    }// end detailsPanel

    // content pane for main dialog
    private void createContentPane()
    {
        setTitle("Employee Details");
        JPanel dialog = new JPanel(new MigLayout());

        setJMenuBar(menuBar());// add menu bar to frame
        // add search panel to frame
        dialog.add(searchPanel(), "width 400:400:400, growx, pushx");
        // add navigation panel to frame
        dialog.add(navigPanel(), "width 150:150:150, wrap");
        // add button panel to frame
        dialog.add(buttonPanel(), "growx, pushx, span 2,wrap");
        // add details panel to frame
        dialog.add(detailsPanel(), "gap top 30, gap left 150, center");

        JScrollPane scrollPane = new JScrollPane(dialog);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        addWindowListener(this);
    }// end createContentPane

    public Employee getCurrentEmployee()
    {
        try
        {
            int id = Integer.parseInt(idField.getText().trim());
            return controller.searchEmployeeById(id);
        }
        catch (Exception e){return null;}
    }

    // display current Employee details
    public void displayRecords(Employee thisEmployee)
    {
        int countGender = 0;
        int countDep = 0;
        boolean found = false;

        searchByIdField.setText("");
        searchBySurnameField.setText("");

        if (thisEmployee == null) {}
        else if (thisEmployee.getEmployeeId() == 0){}
        else
        {
            // find corresponding gender combo box value to current employee
            while (!found && countGender < gender.length - 1)
            {
                if (Character.toString(thisEmployee.getGender()).equalsIgnoreCase(gender[countGender]))
                    found = true;
                else
                    countGender++;
            }
            found = false;
            // find corresponding department combo box value to current employee
            while (!found && countDep < department.length - 1)
            {
                if (thisEmployee.getDepartment().trim().equalsIgnoreCase(department[countDep]))
                    found = true;
                else
                    countDep++;
            }
            idField.setText(Integer.toString(thisEmployee.getEmployeeId()));
            ppsField.setText(thisEmployee.getPps().trim());
            surnameField.setText(thisEmployee.getSurname().trim());
            firstNameField.setText(thisEmployee.getFirstName());
            genderCombo.setSelectedIndex(countGender);
            departmentCombo.setSelectedIndex(countDep);
            salaryField.setText(format.format(thisEmployee.getSalary()));
            // set corresponding full time combo box value to current employee
            if (thisEmployee.getFullTime())
                fullTimeCombo.setSelectedIndex(1);
            else
                fullTimeCombo.setSelectedIndex(2);
        }
        change = false;
    }

    private void displayEmployeeSummaryDialog()
    {
        if (isSomeoneToDisplay())
            new EmployeeSummaryDialog(controller.getAllEmployees());
    }

    private void displaySearchByIdDialog()
    {
        if (isSomeoneToDisplay())
            new SearchByIdDialog(EmployeeDetails.this);
    }

    private void displaySearchBySurnameDialog()
    {
        if (isSomeoneToDisplay())
            new SearchBySurnameDialog(EmployeeDetails.this);
    }

    public void searchEmployeeById()
    {
        try
        {
            int id = Integer.parseInt(searchByIdField.getText().trim());
            Employee emp = controller.searchEmployeeById(id);

            if(emp == null)
                JOptionPane.showMessageDialog(null, "Employee not found!");
            else
                displayRecords(emp);
        }
        catch(NumberFormatException e)
        {
            searchByIdField.setBackground(new Color(255, 150, 150));
            JOptionPane.showMessageDialog(null, "Wrong ID format!");
        }

        searchByIdField.setBackground(Color.WHITE);
        searchByIdField.setText("");
    }

    // get next free ID from Employees in the file
    public int getNextFreeId()
    {
        int nextFreeId = 0;
        // if file is empty or all records are empty start with ID 1 else look
        // for last active record
        if (controller.file.length() == 0 || !isSomeoneToDisplay())
            nextFreeId++;
        else
        {
            nextFreeId = controller.getNextId();
        }
        return nextFreeId;
    }// end getNextFreeId

    // get values from text fields and create Employee object
    private Employee getChangedDetails()
    {
        boolean fullTime = false;
        Employee theEmployee;
        if (((String) fullTimeCombo.getSelectedItem()).equalsIgnoreCase("Yes"))
            fullTime = true;

        theEmployee = new Employee(Integer.parseInt(idField.getText()), ppsField.getText().toUpperCase(),
                surnameField.getText().toUpperCase(), firstNameField.getText().toUpperCase(),
                genderCombo.getSelectedItem().toString().charAt(0), departmentCombo.getSelectedItem().toString(),
                Double.parseDouble(salaryField.getText()), fullTime);

        return theEmployee;
    }

    // delete (make inactive - empty) record from file
    private void deleteRecord()
    {
        if (isSomeoneToDisplay())
        {	// if any active record in file display
            // message and delete record
            int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to delete record?", "Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            // if answer yes delete (make inactive - empty) record
            if (returnVal == JOptionPane.YES_OPTION)
            {
                controller.deleteRecord();

                if (isSomeoneToDisplay())
                {
                    displayRecords(controller.getNextEmployee());
                }
            }
        }
    }

    // activate field for editing
    private void editDetails()
    {
        // activate field for editing if there is records to display
        if (isSomeoneToDisplay())
        {
            // remove euro sign from salary text field
            salaryField.setText(fieldFormat.format(controller.searchEmployeeById(Integer.parseInt(idField.getText().trim())).getSalary()));
            change = false;
            setEnabled(true);// enable text fields for editing
        } // end if
    }// end editDetails

    // ignore changes and set text field unenabled
    private void cancelChange()
    {
        setEnabled(false);
        displayRecords(getCurrentEmployee());
    }// end cancelChange

    // check if any of records in file is active - ID is not 0
    private boolean isSomeoneToDisplay()
    {
        boolean someoneToDisplay = controller.recordsAvailable();

        if (!someoneToDisplay)
            clearFields();

        return someoneToDisplay;
    }

    // check for correct PPS format and look if PPS already in use
    public boolean correctPps(String pps, long currentByte)
    {
        boolean ppsExist = false;

        if (Extensions.ppsValidity(pps))
        {
            ppsExist = controller.ppsInUse(pps, currentByte);
        }

        return ppsExist;
    }

    // check if any changes text field where made
    private boolean checkForChanges()
    {
        boolean anyChanges = false;
        // if changes where made, allow user to save there changes
        if (change)
        {
            saveChanges();// save changes
            anyChanges = true;
        } // end if
        // if no changes made, set text fields as unenabled and display
        // current Employee
        else
        {
            setEnabled(false);
            displayRecords(getCurrentEmployee());
        } // end else

        return anyChanges;
    }// end checkForChanges

    // check for input in text fields
    private boolean checkInput()
    {
        boolean valid = true;
        // if any of inputs are in wrong format, colour text field and display message
        if (ppsField.isEditable() && ppsField.getText().trim().isEmpty())
        {
            ppsField.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        if (ppsField.isEditable() && correctPps(ppsField.getText().trim(), controller.currentByteStart))
        {
            ppsField.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        if (surnameField.isEditable() && surnameField.getText().trim().isEmpty())
        {
            surnameField.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        if (firstNameField.isEditable() && firstNameField.getText().trim().isEmpty())
        {
            firstNameField.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        if (genderCombo.getSelectedIndex() == 0 && genderCombo.isEnabled())
        {
            genderCombo.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        if (departmentCombo.getSelectedIndex() == 0 && departmentCombo.isEnabled())
        {
            departmentCombo.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        try
        {	// try to get values from text field
            Double.parseDouble(salaryField.getText());
            if (Double.parseDouble(salaryField.getText()) < 0)
            {
                salaryField.setBackground(new Color(255, 150, 150));
                valid = false;
            }
        }
        catch (NumberFormatException num)
        {
            if (salaryField.isEditable())
            {
                salaryField.setBackground(new Color(255, 150, 150));
                valid = false;
            }
        }
        if (fullTimeCombo.getSelectedIndex() == 0 && fullTimeCombo.isEnabled())
        {
            fullTimeCombo.setBackground(new Color(255, 150, 150));
            valid = false;
        }
        // display message if any input or format is wrong
        if (!valid)
            JOptionPane.showMessageDialog(null, "Wrong values or format! Please check!");
        // set text field to white colour if text fields are editable
        if (ppsField.isEditable())
            setToWhite();

        return valid;
    }

    private void setToWhite()
    {
        ppsField.setBackground(UIManager.getColor("TextField.background"));
        surnameField.setBackground(UIManager.getColor("TextField.background"));
        firstNameField.setBackground(UIManager.getColor("TextField.background"));
        salaryField.setBackground(UIManager.getColor("TextField.background"));
        genderCombo.setBackground(UIManager.getColor("TextField.background"));
        departmentCombo.setBackground(UIManager.getColor("TextField.background"));
        fullTimeCombo.setBackground(UIManager.getColor("TextField.background"));
    }

    public void setEnabled(boolean booleanValue)
    {
        boolean search = !booleanValue;
        ppsField.setEditable(booleanValue);
        surnameField.setEditable(booleanValue);
        firstNameField.setEditable(booleanValue);
        genderCombo.setEnabled(booleanValue);
        departmentCombo.setEnabled(booleanValue);
        salaryField.setEditable(booleanValue);
        fullTimeCombo.setEnabled(booleanValue);
        saveChange.setVisible(booleanValue);
        cancelChange.setVisible(booleanValue);
        searchByIdField.setEnabled(search);
        searchBySurnameField.setEnabled(search);
        searchId.setEnabled(search);
        searchSurname.setEnabled(search);
    }

    public void openFile()
    {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open");
        fc.setFileFilter(datfilter);
        File newFile;
        if (controller.file.length() != 0 || change)
        {
            int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                saveFile();
            }
        }

        int returnVal = fc.showOpenDialog(EmployeeDetails.this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            newFile = fc.getSelectedFile();

            if (controller.file.getName().equals(generatedFileName))
                controller.file.delete();
            controller.file = newFile;
        }
    }

    private void saveFile()
    {
        // if file name is generated file name, save file as 'save as' else save
        // changes to file
        if (controller.file.getName().equals(generatedFileName))
            saveFileAs();// save file as 'save as'
        else
        {
            // if changes has been made to text field offer user to save these changes
            if (change)
            {
                int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                // save changes if user choose this option
                if (returnVal == JOptionPane.YES_OPTION)
                {
                    // save changes if ID field is not empty
                    if (!idField.getText().equals(""))
                    {
                        Employee emp = getChangedDetails();
                        controller.updateRecord(emp);
                    }
                }
            }

            displayRecords(getCurrentEmployee());
            setEnabled(false);
        }
    }

    private void saveFileAs()
    {
        final JFileChooser fc = new JFileChooser();
        File newFile;
        String defaultFileName = "new_Employee.dat";
        fc.setDialogTitle("Save As");
        // display files only with .dat extension
        fc.setFileFilter(datfilter);
        fc.setApproveButtonText("Save");
        fc.setSelectedFile(new File(defaultFileName));

        int returnVal = fc.showSaveDialog(EmployeeDetails.this);
        // if file has chosen or written, save old file in new file
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            newFile = fc.getSelectedFile();
            // check for file name
            if (!Extensions.checkFileName(newFile))
            {
                // add .dat extension if it was not there
                newFile = new File(newFile.getAbsolutePath() + ".dat");
                controller.randomFile.createFile(newFile.getAbsolutePath());
            }
            else
                controller.randomFile.createFile(newFile.getAbsolutePath());

            try
            {
                // try to copy old file to new file
                Files.copy(controller.file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // if old file name was generated file name, delete it
                if (controller.file.getName().equals(generatedFileName))
                    controller.file.delete();// delete file
                controller.file = newFile;// assign new file to file
            }
            catch (IOException e) { e.printStackTrace();}
        }
        changesMade = false;
    }

    private void saveChanges()
    {
        int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes to current Employee?", "Save",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        // if user choose to save changes, save changes
        if (returnVal == JOptionPane.YES_OPTION)
        {
            Employee emp = getChangedDetails();
            controller.updateRecord(emp);
            changesMade = false;// state that all changes has bee saved
        }
        displayRecords(getCurrentEmployee());
        setEnabled(false);
    }

    // allow to save changes to file when exiting the application
    private void exitApp()
    {
        // if file is not empty allow to save changes
        if (controller.file.length() != 0)
        {
            if (changesMade)
            {
                int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

                if (returnVal == JOptionPane.YES_OPTION)
                {
                    saveFile();
                    if (controller.file.getName().equals(generatedFileName))
                        controller.file.delete();// delete file
                    System.exit(0);
                }
                else if (returnVal == JOptionPane.NO_OPTION)
                {
                    if (controller.file.getName().equals(generatedFileName))
                        controller.file.delete();// delete file
                    System.exit(0);
                }
            }
            else
            {
                // delete generated file if user chooses not to save file
                if (controller.file.getName().equals(generatedFileName))
                    controller.file.delete();// delete file
                System.exit(0);// exit application
            }
        }
        else
        {
            // delete generated file if user chooses not to save file
            if (controller.file.getName().equals(generatedFileName))
                controller.file.delete();// delete file
            System.exit(0);// exit application
        }
    }

    public void clearFields()
    {
        idField.setText("");
        ppsField.setText("");
        surnameField.setText("");
        firstNameField.setText("");
        salaryField.setText("");
        genderCombo.setSelectedIndex(0);
        departmentCombo.setSelectedIndex(0);
        fullTimeCombo.setSelectedIndex(0);
        JOptionPane.showMessageDialog(null, "No Employees registered!");
    }

    // action listener for buttons, text field and menu items
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == closeApp)
        {
            if (checkInput() && !checkForChanges())
                exitApp();
        }
        else if (e.getSource() == open)
        {
            if (checkInput() && !checkForChanges())
                openFile();
        }
        else if (e.getSource() == save)
        {
            if (checkInput() && !checkForChanges())
                saveFile();
            change = false;
        }
        else if (e.getSource() == saveAs)
        {
            if (checkInput() && !checkForChanges())
                saveFileAs();
            change = false;
        }
        else if (e.getSource() == searchById)
        {
            if (checkInput() && !checkForChanges())
                displaySearchByIdDialog();
        }
        else if (e.getSource() == searchBySurname)
        {
            if (checkInput() && !checkForChanges())
                displaySearchBySurnameDialog();
        }
        else if (e.getSource() == searchId || e.getSource() == searchByIdField)
            searchEmployeeById();
        else if (e.getSource() == searchSurname || e.getSource() == searchBySurnameField)
            controller.searchEmployeeBySurname(searchBySurnameField.getText().trim());
        else if (e.getSource() == saveChange)
        {
            if (checkInput() && !checkForChanges())
                ;
        }
        else if (e.getSource() == cancelChange)
            cancelChange();
        else if (e.getSource() == firstItem || e.getSource() == first)
        {
            if (checkInput() && !checkForChanges())
            {
                displayRecords(controller.getFirstEmployee());
            }
        }
        else if (e.getSource() == prevItem || e.getSource() == previous)
        {
            if (checkInput() && !checkForChanges())
            {
                displayRecords(controller.getPreviousEmployee());
            }
        }
        else if (e.getSource() == nextItem || e.getSource() == next)
        {
            if (checkInput() && !checkForChanges())
            {
                displayRecords(controller.getNextEmployee());
            }
        }
        else if (e.getSource() == lastItem || e.getSource() == last)
        {
            if (checkInput() && !checkForChanges())
            {
                displayRecords(controller.getLastEmployee());
            }
        }
        else if (e.getSource() == listAll || e.getSource() == displayAll)
        {
            if (checkInput() && !checkForChanges())
                if (isSomeoneToDisplay())
                    displayEmployeeSummaryDialog();
        }
        else if (e.getSource() == create || e.getSource() == add)
        {
            if (checkInput() && !checkForChanges())
                new AddRecordDialog(EmployeeDetails.this);
        }
        else if (e.getSource() == modify || e.getSource() == edit)
        {
            if (checkInput() && !checkForChanges())
                editDetails();
        }
        else if (e.getSource() == delete || e.getSource() == deleteButton)
        {
            if (checkInput() && !checkForChanges())
                deleteRecord();
        }
        else if (e.getSource() == searchBySurname)
        {
            if (checkInput() && !checkForChanges())
                new SearchBySurnameDialog(EmployeeDetails.this);
        }
    }

    // create and show main dialog
    private static void createAndShowGUI()
    {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.createContentPane();// add content pane to frame
        frame.setSize(760, 600);
        frame.setLocation(250, 200);
        frame.setVisible(true);
    }

    public static void main(String args[])
    {
        SwingUtilities.invokeLater(EmployeeDetails::createAndShowGUI);
    }// end main

    // DocumentListener methods
    public void changedUpdate(DocumentEvent d)
    {
        change = true;
        new JTextFieldLimit(20);
    }

    public void insertUpdate(DocumentEvent d)
    {
        change = true;
        new JTextFieldLimit(20);
    }

    public void removeUpdate(DocumentEvent d)
    {
        change = true;
        new JTextFieldLimit(20);
    }

    // ItemListener method
    public void itemStateChanged(ItemEvent e)
    {
        change = true;
    }

    // WindowsListener methods
    public void windowClosing(WindowEvent e)
    {
        exitApp();
    }

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}
}// end class EmployeeDetails
