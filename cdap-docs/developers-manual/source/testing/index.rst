.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2014 Cask Data, Inc.

:hide-toc: true

============================================
Testing and Debugging
============================================

.. toctree::
   :maxdepth: 1
   
    Testing <testing>
    Debugging <debugging>

..    Packaging <packaging>


CDAP comes with a number of tools to make a developer's life easier. These tools
help with testing, debugging and packaging CDAP applications:

.. list-table::
    :widths: 25 75
    :header-rows: 1

    * - Tool Name
      - Description
    * - :ref:`Test Framework<TestFramework>`
      - How you can take advantage of the test framework to test your CDAP applications before deploying.
        This makes catching bugs early and easy.
    * - :ref:`Debugging<DebugCDAP>`
      - How you can debug CDAP applications in Standalone mode and app containers in Distributed mode.
    * - :ref:`Debugging the Transactions Manager<TxDebugger>`
      - Covers snapshotting and inspecting the state of the Transaction Manager.
