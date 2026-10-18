"""Validate offline properties of the Azure deployment definition."""
import base64,json,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
class InfrastructureTests(unittest.TestCase):
    def setUp(self):self.template=json.loads((ROOT/'deployment/azure/main.json').read_text())
    def test_cloud_init_matches_the_reviewable_source(self):
        vm=next(r for r in self.template['resources'] if r['type']=='Microsoft.Compute/virtualMachines')
        self.assertEqual(base64.b64decode(vm['properties']['osProfile']['customData']),(ROOT/'deployment/azure/cloud-init.yaml').read_bytes())
        self.assertTrue(vm['properties']['osProfile']['linuxConfiguration']['disablePasswordAuthentication'])
    def test_public_inbound_ports_are_web_and_restricted_ssh(self):
        nsg=next(r for r in self.template['resources'] if r['type']=='Microsoft.Network/networkSecurityGroups')
        rules=nsg['properties']['securityRules']
        self.assertEqual({r['properties']['destinationPortRange'] for r in rules},{'80','443','22'})
        ssh=next(r['properties'] for r in rules if r['properties']['destinationPortRange']=='22')
        self.assertEqual(ssh['sourceAddressPrefix'],"[parameters('sshAllowedCidr')]")
if __name__=='__main__':unittest.main()
